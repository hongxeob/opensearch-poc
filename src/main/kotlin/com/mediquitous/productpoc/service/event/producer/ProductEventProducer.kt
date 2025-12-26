package com.mediquitous.productpoc.service.event.producer

import com.mediquitous.productpoc.service.event.topic.ProductDeletedEvent
import com.mediquitous.productpoc.service.event.topic.ProductTopics
import com.mediquitous.productpoc.service.event.topic.ProductUpdatedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 상품 이벤트 프로듀서 인터페이스
 *
 * Go 서버의 event/producer/product.go 구조 변환
 */
interface ProductEventProducer {
    /**
     * product.updated 이벤트 발행
     */
    fun sendUpdated(id: Long)

    /**
     * product.deleted 이벤트 발행
     */
    fun sendDeleted(id: Long)
}

/**
 * Kafka 기반 상품 이벤트 프로듀서 구현체
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class KafkaProductEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) : ProductEventProducer {
    override fun sendUpdated(id: Long) {
        val event = ProductUpdatedEvent(id = id)

        try {
            kafkaTemplate
                .send(ProductTopics.PRODUCT_UPDATED, event.key(), event)
                .whenComplete { result, ex ->
                    if (ex != null) {
                        logger.error(ex) { "product.updated 이벤트 발행 실패: productId=$id" }
                    } else {
                        logger.debug {
                            "product.updated 이벤트 발행 성공: productId=$id, " +
                                "partition=${result.recordMetadata.partition()}, " +
                                "offset=${result.recordMetadata.offset()}"
                        }
                    }
                }
        } catch (e: Exception) {
            logger.error(e) { "product.updated 이벤트 발행 중 예외: productId=$id" }
            throw e
        }
    }

    override fun sendDeleted(id: Long) {
        val event = ProductDeletedEvent(id = id)

        try {
            kafkaTemplate
                .send(ProductTopics.PRODUCT_DELETED, event.key(), event)
                .whenComplete { result, ex ->
                    if (ex != null) {
                        logger.error(ex) { "product.deleted 이벤트 발행 실패: productId=$id" }
                    } else {
                        logger.debug {
                            "product.deleted 이벤트 발행 성공: productId=$id, " +
                                "partition=${result.recordMetadata.partition()}, " +
                                "offset=${result.recordMetadata.offset()}"
                        }
                    }
                }
        } catch (e: Exception) {
            logger.error(e) { "product.deleted 이벤트 발행 중 예외: productId=$id" }
            throw e
        }
    }
}

/**
 * Kafka 비활성화 시 사용되는 No-op 구현체
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "false", matchIfMissing = true)
class NoOpProductEventProducer : ProductEventProducer {
    override fun sendUpdated(id: Long) {
        logger.debug { "[NoOp] product.updated 이벤트 스킵: productId=$id (Kafka 비활성화)" }
    }

    override fun sendDeleted(id: Long) {
        logger.debug { "[NoOp] product.deleted 이벤트 스킵: productId=$id (Kafka 비활성화)" }
    }
}
