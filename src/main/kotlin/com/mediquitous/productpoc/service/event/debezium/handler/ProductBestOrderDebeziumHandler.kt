package com.mediquitous.productpoc.service.event.debezium.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.service.event.ProductEventBuffer
import com.mediquitous.productpoc.service.event.debezium.ChangeEvent
import com.mediquitous.productpoc.service.event.debezium.DebeziumTopics
import com.mediquitous.productpoc.service.event.debezium.ProductBestOrderPayload
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
 * shopping_productbestorder 테이블 CDC 핸들러
 *
 * Go 서버의 internal/event/handler/debezium/product_best_order.go 변환
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class ProductBestOrderDebeziumHandler(
    private val objectMapper: ObjectMapper,
    private val productEventBuffer: ProductEventBuffer,
) {
    @KafkaListener(
        topics = [DebeziumTopics.PRODUCT_BEST_ORDER],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handle(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "ProductBestOrder CDC 수신: partition=$partition, offset=$offset" }

        try {
            val event =
                objectMapper.readValue(
                    payload,
                    object : TypeReference<ChangeEvent<ProductBestOrderPayload>>() {},
                )
            val record = event.record()

            productEventBuffer.add(listOf(record.productId))
            acknowledgment.acknowledge()

            logger.debug { "ProductBestOrder 처리 완료: productId=${record.productId}" }
        } catch (e: Exception) {
            logger.error(e) { "ProductBestOrder CDC 처리 실패" }
            throw e
        }
    }
}
