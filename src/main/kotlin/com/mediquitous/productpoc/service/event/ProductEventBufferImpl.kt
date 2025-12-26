package com.mediquitous.productpoc.service.event

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 상품 이벤트 버퍼 구현체
 *
 * Go 서버의 event_buffer.go 로직을 Kotlin으로 변환
 */
@Component
class ProductEventBufferImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val kafkaTemplate: KafkaTemplate<String, ProductUpdatedEvent>,
) : ProductEventBuffer {
    companion object {
        private const val BUFFER_KEY = "buffer:event:products"
        private const val TOPIC_PRODUCT_UPDATED = "product.updated"
    }

    override fun add(productIds: List<Long>) {
        if (productIds.isEmpty()) {
            logger.debug { "빈 상품 ID 목록, 버퍼 추가 건너뜀" }
            return
        }

        try {
            // Redis List의 오른쪽에 추가 (RPUSH)
            val stringIds = productIds.map { it.toString() }
            redisTemplate.opsForList().rightPushAll(BUFFER_KEY, stringIds)

            logger.info { "상품 ID ${productIds.size}개를 버퍼에 추가: $productIds" }
        } catch (e: Exception) {
            logger.error(e) { "상품 ID 버퍼 추가 실패: $productIds" }
            throw RuntimeException("Failed to add product IDs to buffer", e)
        }
    }

    override fun flush(count: Int) {
        logger.info { "이벤트 버퍼 flush 시작: count=$count" }

        try {
            // Redis List의 왼쪽에서 count개 꺼내기 (LPOP)
            val productIdStrings = mutableListOf<String>()
            repeat(count) {
                val id = redisTemplate.opsForList().leftPop(BUFFER_KEY)
                if (id != null) {
                    productIdStrings.add(id)
                } else {
                    // 버퍼가 비었으면 중단
                    return@repeat
                }
            }

            if (productIdStrings.isEmpty()) {
                logger.debug { "버퍼에 flush할 항목이 없음" }
                return
            }

            logger.info { "버퍼에서 ${productIdStrings.size}개 항목 추출" }

            // 중복 제거 및 Long 변환
            val uniqueProductIds =
                productIdStrings
                    .mapNotNull {
                        try {
                            it.toLong()
                        } catch (e: NumberFormatException) {
                            logger.warn { "잘못된 상품 ID 형식: $it" }
                            null
                        }
                    }.toSet()

            logger.info { "중복 제거 후 ${uniqueProductIds.size}개 상품 이벤트 발행" }

            // Kafka로 product.updated 이벤트 발행
            uniqueProductIds.forEach { productId ->
                try {
                    val event = ProductUpdatedEvent(id = productId)
                    kafkaTemplate.send(TOPIC_PRODUCT_UPDATED, productId.toString(), event)
                    logger.debug { "product.updated 이벤트 발행: productId=$productId" }
                } catch (e: Exception) {
                    logger.error(e) { "product.updated 이벤트 발행 실패: productId=$productId" }
                    // 실패해도 계속 진행 (Go 서버와 동일)
                }
            }

            logger.info { "이벤트 버퍼 flush 완료: 발행된 이벤트 ${uniqueProductIds.size}개" }
        } catch (e: Exception) {
            logger.error(e) { "이벤트 버퍼 flush 중 오류 발생" }
            throw RuntimeException("Failed to flush event buffer", e)
        }
    }
}

/**
 * Kafka 이벤트 DTO
 */
data class ProductUpdatedEvent(
    val id: Long,
)
