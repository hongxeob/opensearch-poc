package com.mediquitous.productpoc.service.event.debezium.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.service.event.ProductEventBuffer
import com.mediquitous.productpoc.service.event.debezium.ChangeEvent
import com.mediquitous.productpoc.service.event.debezium.DebeziumTopics
import com.mediquitous.productpoc.service.event.debezium.ProductPayload
import com.mediquitous.productpoc.service.event.producer.ProductEventProducer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * shopping_product 테이블 CDC 핸들러
 *
 * Go 서버의 internal/event/handler/debezium/product_handler.go 변환
 *
 * - CREATE/UPDATE: 상품 ID를 버퍼에 추가
 * - DELETE: product.deleted 이벤트 발행
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class ProductDebeziumHandler(
    private val objectMapper: ObjectMapper,
    private val productEventBuffer: ProductEventBuffer,
    private val productEventProducer: ProductEventProducer,
) {
    @KafkaListener(
        topics = [DebeziumTopics.PRODUCT],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handle(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "Product CDC 수신: key=$key, partition=$partition, offset=$offset" }

        try {
            val event =
                objectMapper.readValue(
                    payload,
                    object : TypeReference<ChangeEvent<ProductPayload>>() {},
                )
            val record = event.record()

            if (event.isDelete()) {
                productEventProducer.sendDeleted(record.id)
                logger.info { "Product 삭제 이벤트 발행: productId=${record.id}" }
            } else {
                productEventBuffer.add(listOf(record.id))
                logger.debug { "Product ID 버퍼 추가: productId=${record.id}" }
            }

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error(e) { "Product CDC 처리 실패: payload=$payload" }
            throw e
        }
    }
}
