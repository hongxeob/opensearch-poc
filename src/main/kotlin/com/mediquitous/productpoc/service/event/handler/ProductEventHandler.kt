package com.mediquitous.productpoc.service.event.handler

import com.mediquitous.productpoc.service.event.topic.ProductDeletedEvent
import com.mediquitous.productpoc.service.event.topic.ProductTopics
import com.mediquitous.productpoc.service.event.topic.ProductUpdatedEvent
import com.mediquitous.productpoc.service.index.ProductIndexService
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
 * 상품 이벤트 핸들러 (Kafka Consumer)
 *
 * Go 서버의 event/handler/product_updated.go, product_deleted.go 구조 변환
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class ProductEventHandler(
    private val productIndexService: ProductIndexService,
) {
    /**
     * product.updated 이벤트 처리
     *
     * OpenSearch 인덱스에 상품 정보 업데이트
     */
    @KafkaListener(
        topics = [ProductTopics.PRODUCT_UPDATED],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleProductUpdated(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "product.updated 이벤트 수신: key=$key, partition=$partition, offset=$offset" }

        try {
            val event = parseProductUpdatedEvent(payload)

            productIndexService.updateProduct(event.id)

            acknowledgment.acknowledge()
            logger.info { "product.updated 처리 완료: productId=${event.id}" }
        } catch (e: Exception) {
            logger.error(e) { "product.updated 처리 실패: payload=$payload" }
            // 재시도 정책에 따라 처리 (DLQ로 보내거나 재시도)
            throw e
        }
    }

    /**
     * product.deleted 이벤트 처리
     *
     * OpenSearch 인덱스에서 상품 삭제
     */
    @KafkaListener(
        topics = [ProductTopics.PRODUCT_DELETED],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleProductDeleted(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "product.deleted 이벤트 수신: key=$key, partition=$partition, offset=$offset" }

        try {
            val event = parseProductDeletedEvent(payload)

            productIndexService.deleteProduct(event.id)

            acknowledgment.acknowledge()
            logger.info { "product.deleted 처리 완료: productId=${event.id}" }
        } catch (e: Exception) {
            logger.error(e) { "product.deleted 처리 실패: payload=$payload" }
            throw e
        }
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    private fun parseProductUpdatedEvent(payload: String): ProductUpdatedEvent {
        // 간단한 JSON 파싱 (Jackson ObjectMapper 사용 권장)
        val idRegex = """"id"\s*:\s*(\d+)""".toRegex()
        val matchResult = idRegex.find(payload)
        val id =
            matchResult?.groupValues?.get(1)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid ProductUpdatedEvent payload: $payload")

        return ProductUpdatedEvent(id = id)
    }

    private fun parseProductDeletedEvent(payload: String): ProductDeletedEvent {
        val idRegex = """"id"\s*:\s*(\d+)""".toRegex()
        val matchResult = idRegex.find(payload)
        val id =
            matchResult?.groupValues?.get(1)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid ProductDeletedEvent payload: $payload")

        return ProductDeletedEvent(id = id)
    }
}
