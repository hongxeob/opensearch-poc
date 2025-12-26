package com.mediquitous.productpoc.event.producer

import com.mediquitous.productpoc.event.topic.ProductTopics
import com.mediquitous.productpoc.event.topic.SellerDeletedEvent
import com.mediquitous.productpoc.event.topic.SellerUpdatedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 셀러 이벤트 프로듀서 인터페이스
 *
 * Go 서버의 event/producer/seller.go 구조 변환
 */
interface SellerEventProducer {
    /**
     * seller.updated 이벤트 발행
     */
    fun sendUpdated(id: Long)

    /**
     * seller.deleted 이벤트 발행
     */
    fun sendDeleted(id: Long)
}

/**
 * Kafka 기반 셀러 이벤트 프로듀서 구현체
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class KafkaSellerEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) : SellerEventProducer {
    override fun sendUpdated(id: Long) {
        val event = SellerUpdatedEvent(id = id)

        try {
            kafkaTemplate
                .send(ProductTopics.SELLER_UPDATED, event.key(), event)
                .whenComplete { result, ex ->
                    if (ex != null) {
                        logger.error(ex) { "seller.updated 이벤트 발행 실패: sellerId=$id" }
                    } else {
                        logger.debug {
                            "seller.updated 이벤트 발행 성공: sellerId=$id, " +
                                "partition=${result.recordMetadata.partition()}, " +
                                "offset=${result.recordMetadata.offset()}"
                        }
                    }
                }
        } catch (e: Exception) {
            logger.error(e) { "seller.updated 이벤트 발행 중 예외: sellerId=$id" }
            throw e
        }
    }

    override fun sendDeleted(id: Long) {
        val event = SellerDeletedEvent(id = id)

        try {
            kafkaTemplate
                .send(ProductTopics.SELLER_DELETED, event.key(), event)
                .whenComplete { result, ex ->
                    if (ex != null) {
                        logger.error(ex) { "seller.deleted 이벤트 발행 실패: sellerId=$id" }
                    } else {
                        logger.debug {
                            "seller.deleted 이벤트 발행 성공: sellerId=$id, " +
                                "partition=${result.recordMetadata.partition()}, " +
                                "offset=${result.recordMetadata.offset()}"
                        }
                    }
                }
        } catch (e: Exception) {
            logger.error(e) { "seller.deleted 이벤트 발행 중 예외: sellerId=$id" }
            throw e
        }
    }
}

/**
 * Kafka 비활성화 시 사용되는 No-op 구현체
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "false", matchIfMissing = true)
class NoOpSellerEventProducer : SellerEventProducer {
    override fun sendUpdated(id: Long) {
        logger.debug { "[NoOp] seller.updated 이벤트 스킵: sellerId=$id (Kafka 비활성화)" }
    }

    override fun sendDeleted(id: Long) {
        logger.debug { "[NoOp] seller.deleted 이벤트 스킵: sellerId=$id (Kafka 비활성화)" }
    }
}
