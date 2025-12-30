package com.mediquitous.productpoc.service.event.debezium.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.repository.jpa.product.ProductJpaRepository
import com.mediquitous.productpoc.service.event.ProductEventBuffer
import com.mediquitous.productpoc.service.event.debezium.ChangeEvent
import com.mediquitous.productpoc.service.event.debezium.DebeziumTopics
import com.mediquitous.productpoc.service.event.debezium.SellerPayload
import com.mediquitous.productpoc.service.event.producer.SellerEventProducer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

private const val PAGE_SIZE = 1000

/**
 * shopping_seller 테이블 CDC 핸들러
 *
 * Go 서버의 internal/event/handler/debezium/seller_handler.go 변환
 *
 * Seller 변경 시:
 * 1. seller.updated 이벤트 발행
 * 2. 해당 Seller의 모든 상품 ID를 버퍼에 추가 (커서 페이징)
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class SellerDebeziumHandler(
    private val objectMapper: ObjectMapper,
    private val productEventBuffer: ProductEventBuffer,
    private val sellerEventProducer: SellerEventProducer,
    private val productJpaRepository: ProductJpaRepository,
) {
    @KafkaListener(
        topics = [DebeziumTopics.SELLER],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handle(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "Seller CDC 수신: partition=$partition, offset=$offset" }

        try {
            val event =
                objectMapper.readValue(
                    payload,
                    object : TypeReference<ChangeEvent<SellerPayload>>() {},
                )
            val record = event.record()

            if (record.id <= 0) {
                logger.warn { "Invalid seller ID: ${record.id}" }
                acknowledgment.acknowledge()
                return
            }

            // 1. seller.updated 이벤트 발행
            sellerEventProducer.sendUpdated(record.id)

            // 2. 해당 Seller의 모든 상품 ID를 버퍼에 추가 (커서 페이징)
            var afterId = 0L
            while (true) {
                val productIds =
                    productJpaRepository.findProductIdsBySellerId(
                        sellerId = record.id,
                        afterId = afterId,
                        limit = PAGE_SIZE,
                    )

                if (productIds.isEmpty()) break

                productEventBuffer.add(productIds)
                logger.debug { "Seller 상품 버퍼 추가: sellerId=${record.id}, count=${productIds.size}" }

                if (productIds.size < PAGE_SIZE) break
                afterId = productIds.last()
            }

            acknowledgment.acknowledge()
            logger.info { "Seller CDC 처리 완료: sellerId=${record.id}" }
        } catch (e: Exception) {
            logger.error(e) { "Seller CDC 처리 실패" }
            throw e
        }
    }
}
