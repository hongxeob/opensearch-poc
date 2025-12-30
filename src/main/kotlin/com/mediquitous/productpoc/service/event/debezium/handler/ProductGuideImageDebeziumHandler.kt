package com.mediquitous.productpoc.service.event.debezium.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.repository.jpa.product.ProductJpaRepository
import com.mediquitous.productpoc.service.event.ProductEventBuffer
import com.mediquitous.productpoc.service.event.debezium.ChangeEvent
import com.mediquitous.productpoc.service.event.debezium.DebeziumTopics
import com.mediquitous.productpoc.service.event.debezium.ProductGuideImagePayload
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
 * shopping_productguideimage 테이블 CDC 핸들러
 *
 * Go 서버의 internal/event/handler/debezium/product_guide_image_handler.go 변환
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class ProductGuideImageDebeziumHandler(
    private val objectMapper: ObjectMapper,
    private val productEventBuffer: ProductEventBuffer,
    private val productJpaRepository: ProductJpaRepository,
) {
    @KafkaListener(
        topics = [DebeziumTopics.PRODUCT_GUIDE_IMAGE],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handle(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "ProductGuideImage CDC 수신: partition=$partition, offset=$offset" }

        try {
            val event =
                objectMapper.readValue(
                    payload,
                    object : TypeReference<ChangeEvent<ProductGuideImagePayload>>() {},
                )
            val record = event.record()

            // GuideImage ID로 Product 조회
            val product = productJpaRepository.findByGuideImageId(record.id)
            if (product != null) {
                productEventBuffer.add(listOf(product.id!!))
                logger.debug { "ProductGuideImage 처리 완료: guideImageId=${record.id}, productId=${product.id}" }
            } else {
                logger.warn { "ProductGuideImage: 해당 상품 없음: guideImageId=${record.id}" }
            }

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error(e) { "ProductGuideImage CDC 처리 실패" }
            throw e
        }
    }
}
