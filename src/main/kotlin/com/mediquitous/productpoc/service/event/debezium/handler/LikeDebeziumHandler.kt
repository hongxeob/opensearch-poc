package com.mediquitous.productpoc.service.event.debezium.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.service.event.debezium.ChangeEvent
import com.mediquitous.productpoc.service.event.debezium.DebeziumTopics
import com.mediquitous.productpoc.service.event.debezium.LikePayload
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

/**
 * shopping_like 테이블 CDC 핸들러
 *
 * Go 서버의 internal/event/handler/debezium/like_handler.go 변환
 *
 * Seller Like인 경우에만 seller.updated 이벤트 발행
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class LikeDebeziumHandler(
    private val objectMapper: ObjectMapper,
    private val sellerEventProducer: SellerEventProducer,
) {
    @KafkaListener(
        topics = [DebeziumTopics.LIKE],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handle(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "Like CDC 수신: partition=$partition, offset=$offset" }

        try {
            val event =
                objectMapper.readValue(
                    payload,
                    object : TypeReference<ChangeEvent<LikePayload>>() {},
                )
            val record = event.record()

            // Seller Like인 경우에만 처리
            val sellerId = record.sellerId
            if (sellerId == null || sellerId <= 0) {
                logger.debug { "Not a seller like event: likeId=${record.id}" }
                acknowledgment.acknowledge()
                return
            }

            sellerEventProducer.sendUpdated(sellerId)

            acknowledgment.acknowledge()
            logger.info { "Like (Seller) CDC 처리 완료: sellerId=$sellerId" }
        } catch (e: Exception) {
            logger.error(e) { "Like CDC 처리 실패" }
            throw e
        }
    }
}
