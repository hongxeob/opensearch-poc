package com.mediquitous.productpoc.event.handler

import com.mediquitous.productpoc.event.topic.ProductTopics
import com.mediquitous.productpoc.event.topic.SellerDeletedEvent
import com.mediquitous.productpoc.event.topic.SellerUpdatedEvent
import com.mediquitous.productpoc.service.SellerIndexService
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
 * 셀러 이벤트 핸들러 (Kafka Consumer)
 *
 * Go 서버의 event/handler/seller_updated.go 구조 변환
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class SellerEventHandler(
    private val sellerIndexService: SellerIndexService,
) {
    /**
     * seller.updated 이벤트 처리
     *
     * OpenSearch 인덱스에 셀러 정보 업데이트
     */
    @KafkaListener(
        topics = [ProductTopics.SELLER_UPDATED],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleSellerUpdated(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "seller.updated 이벤트 수신: key=$key, partition=$partition, offset=$offset" }

        try {
            val event = parseSellerUpdatedEvent(payload)

            sellerIndexService.updateSeller(event.id)

            acknowledgment.acknowledge()
            logger.info { "seller.updated 처리 완료: sellerId=${event.id}" }
        } catch (e: Exception) {
            logger.error(e) { "seller.updated 처리 실패: payload=$payload" }
            throw e
        }
    }

    /**
     * seller.deleted 이벤트 처리
     *
     * OpenSearch 인덱스에서 셀러 삭제
     */
    @KafkaListener(
        topics = [ProductTopics.SELLER_DELETED],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleSellerDeleted(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "seller.deleted 이벤트 수신: key=$key, partition=$partition, offset=$offset" }

        try {
            val event = parseSellerDeletedEvent(payload)

            sellerIndexService.deleteSeller(event.id)

            acknowledgment.acknowledge()
            logger.info { "seller.deleted 처리 완료: sellerId=${event.id}" }
        } catch (e: Exception) {
            logger.error(e) { "seller.deleted 처리 실패: payload=$payload" }
            throw e
        }
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    private fun parseSellerUpdatedEvent(payload: String): SellerUpdatedEvent {
        val idRegex = """"id"\s*:\s*(\d+)""".toRegex()
        val matchResult = idRegex.find(payload)
        val id =
            matchResult?.groupValues?.get(1)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid SellerUpdatedEvent payload: $payload")

        return SellerUpdatedEvent(id = id)
    }

    private fun parseSellerDeletedEvent(payload: String): SellerDeletedEvent {
        val idRegex = """"id"\s*:\s*(\d+)""".toRegex()
        val matchResult = idRegex.find(payload)
        val id =
            matchResult?.groupValues?.get(1)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid SellerDeletedEvent payload: $payload")

        return SellerDeletedEvent(id = id)
    }
}
