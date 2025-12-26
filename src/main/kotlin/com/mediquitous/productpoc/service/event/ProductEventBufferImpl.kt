package com.mediquitous.productpoc.service.event

import com.mediquitous.productpoc.event.producer.ProductEventProducer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 상품 이벤트 버퍼 구현체
 *
 * Go 서버의 event_buffer.go 로직을 Kotlin으로 변환
 * Redis를 버퍼로 사용하여 배치 처리
 */
@Component
@ConditionalOnProperty(prefix = "redis", name = ["enabled"], havingValue = "true")
class ProductEventBufferImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val productEventProducer: ProductEventProducer,
) : ProductEventBuffer {
    companion object {
        private const val BUFFER_KEY = "buffer:event:products"
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

            // ProductEventProducer를 통해 이벤트 발행
            uniqueProductIds.forEach { productId ->
                try {
                    productEventProducer.sendUpdated(productId)
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
 * Redis 비활성화 시 사용되는 No-op 구현체
 */
@Component
@ConditionalOnProperty(prefix = "redis", name = ["enabled"], havingValue = "false", matchIfMissing = true)
class NoOpProductEventBufferImpl : ProductEventBuffer {
    override fun add(productIds: List<Long>) {
        logger.debug { "[NoOp] 상품 ID 버퍼 추가 스킵: $productIds (Redis 비활성화)" }
    }

    override fun flush(count: Int) {
        logger.debug { "[NoOp] 이벤트 버퍼 flush 스킵: count=$count (Redis 비활성화)" }
    }
}
