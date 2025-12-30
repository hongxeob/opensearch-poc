package com.mediquitous.productpoc.service.event.debezium.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.repository.jpa.product.ProductVariantJpaRepository
import com.mediquitous.productpoc.service.event.ProductEventBuffer
import com.mediquitous.productpoc.service.event.debezium.ChangeEvent
import com.mediquitous.productpoc.service.event.debezium.DebeziumTopics
import com.mediquitous.productpoc.service.event.debezium.ProductVariantOptionSetPayload
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
 * shopping_productvariant_option_set 테이블 CDC 핸들러
 *
 * Go 서버의 internal/event/handler/debezium/product_variant_option_set_handler.go 변환
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class ProductVariantOptionSetDebeziumHandler(
    private val objectMapper: ObjectMapper,
    private val productEventBuffer: ProductEventBuffer,
    private val productVariantJpaRepository: ProductVariantJpaRepository,
) {
    @KafkaListener(
        topics = [DebeziumTopics.PRODUCT_VARIANT_OPTION_SET],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handle(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "ProductVariantOptionSet CDC 수신: partition=$partition, offset=$offset" }

        try {
            val event =
                objectMapper.readValue(
                    payload,
                    object : TypeReference<ChangeEvent<ProductVariantOptionSetPayload>>() {},
                )
            val record = event.record()

            // VariantId로 ProductVariant 조회 → productId 획득
            val productVariant = productVariantJpaRepository.findNullableById(record.variantId)
            if (productVariant?.productId != null) {
                productEventBuffer.add(listOf(productVariant.productId))
                logger.debug { "ProductVariantOptionSet 처리 완료: variantId=${record.variantId}, productId=${productVariant.productId}" }
            } else {
                logger.warn { "ProductVariantOptionSet: ProductVariant 없음 또는 productId 없음: variantId=${record.variantId}" }
            }

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error(e) { "ProductVariantOptionSet CDC 처리 실패" }
            throw e
        }
    }
}
