package com.mediquitous.productpoc.service

import com.mediquitous.productpoc.repository.jpa.seller.SellerJpaRepository
import com.mediquitous.productpoc.service.event.producer.SellerEventProducer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 셀러 마이그레이션 서비스 구현체
 *
 * Go 서버의 seller/migration_service.go 로직을 Kotlin으로 변환
 */
@Service
@Transactional(readOnly = true)
class SellerMigrationServiceImpl(
    private val sellerJpaRepository: SellerJpaRepository,
    private val sellerEventProducer: SellerEventProducer,
) : SellerMigrationService {
    companion object {
        private const val BATCH_SIZE = 1000
        private const val TOPIC_SELLER_UPDATED = "seller.updated"
    }

    override fun migrateAll() {
        logger.info { "전체 셀러 마이그레이션 시작" }

        val allSellerIds = mutableListOf<Long>()
        var lastId = 0L

        try {
            // 1. 모든 셀러 ID 수집 (배치 단위)
            while (true) {
                val sellerIds = sellerJpaRepository.findSellerIdsAfter(lastId, BATCH_SIZE)

                if (sellerIds.isEmpty()) {
                    logger.info { "더 이상 조회할 셀러가 없음" }
                    break
                }

                logger.info { "셀러 ID ${sellerIds.size}개 조회됨: ${sellerIds.first()}..${sellerIds.last()}" }

                allSellerIds.addAll(sellerIds)

                if (sellerIds.size < BATCH_SIZE) {
                    logger.info { "마지막 배치 처리 완료" }
                    break
                }

                lastId = sellerIds.last()
            }

            logger.info { "총 ${allSellerIds.size}개 셀러 ID 수집 완료" }

            // 2. 각 셀러 ID에 대해 Kafka 이벤트 발행
            allSellerIds.forEach { sellerId ->
                try {
                    sellerEventProducer.sendUpdated(sellerId)
                    logger.debug { "seller.updated 이벤트 발행: sellerId=$sellerId" }
                } catch (e: Exception) {
                    // 실패해도 계속 진행 (Go 서버와 동일)
                    logger.error(e) { "seller.updated 이벤트 발행 실패: sellerId=$sellerId, 계속 진행" }
                }
            }

            logger.info { "전체 셀러 마이그레이션 완료: 총 ${allSellerIds.size}개 셀러 처리" }
        } catch (e: Exception) {
            logger.error(e) { "전체 셀러 마이그레이션 중 오류 발생" }
            throw RuntimeException("Failed to migrate all sellers", e)
        }
    }
}

/**
 * Kafka 이벤트 DTO
 */
data class SellerUpdatedEvent(
    val id: Long,
)
