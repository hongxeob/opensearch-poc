package com.mediquitous.productpoc.service

import com.mediquitous.productpoc.repository.jpa.product.ProductJpaRepository
import com.mediquitous.productpoc.service.event.ProductEventBuffer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 상품 마이그레이션 서비스 구현체
 *
 * Go 서버의 migration_service.go 로직을 Kotlin으로 변환
 */
@Service
@Transactional(readOnly = true)
class ProductMigrationServiceImpl(
    private val productJpaRepository: ProductJpaRepository,
    private val productEventBuffer: ProductEventBuffer,
) : ProductMigrationService {
    companion object {
        private const val BATCH_SIZE = 1000
    }

    override fun migrateAll() {
        logger.info { "전체 상품 마이그레이션 시작" }

        var lastId = 0L
        var totalProcessed = 0

        try {
            while (true) {
                // 배치 단위로 상품 ID 조회 (커서 기반 페이지네이션)
                val productIds = productJpaRepository.findProductIdsAfter(lastId, BATCH_SIZE)

                if (productIds.isEmpty()) {
                    logger.info { "더 이상 조회할 상품이 없음, 마이그레이션 종료" }
                    break
                }

                logger.info { "상품 ID ${productIds.size}개 조회됨: ${productIds.first()}..${productIds.last()}" }

                // 이벤트 버퍼에 추가
                productEventBuffer.add(productIds)

                totalProcessed += productIds.size

                // 다음 배치를 위한 커서 갱신
                if (productIds.size < BATCH_SIZE) {
                    logger.info { "마지막 배치 처리 완료" }
                    break
                }

                lastId = productIds.last()
            }

            logger.info { "전체 상품 마이그레이션 완료: 총 ${totalProcessed}개 상품 처리" }
        } catch (e: Exception) {
            logger.error(e) { "전체 상품 마이그레이션 중 오류 발생: 처리된 상품 ${totalProcessed}개" }
            throw RuntimeException("Failed to migrate all products", e)
        }
    }

    override fun migrateByIds(productIds: List<Long>) {
        if (productIds.isEmpty()) {
            logger.warn { "빈 상품 ID 목록, 마이그레이션 건너뜀" }
            return
        }

        logger.info { "특정 상품 마이그레이션 시작: ${productIds.size}개" }

        try {
            // 이벤트 버퍼에 추가
            productEventBuffer.add(productIds)

            logger.info { "특정 상품 마이그레이션 완료: $productIds" }
        } catch (e: Exception) {
            logger.error(e) { "특정 상품 마이그레이션 실패: $productIds" }
            throw RuntimeException("Failed to migrate products by IDs", e)
        }
    }

    override fun flushEventBuffer(count: Int) {
        logger.info { "이벤트 버퍼 flush 요청: count=$count" }

        try {
            productEventBuffer.flush(count)
            logger.info { "이벤트 버퍼 flush 완료" }
        } catch (e: Exception) {
            logger.error(e) { "이벤트 버퍼 flush 실패" }
            throw RuntimeException("Failed to flush event buffer", e)
        }
    }
}
