package com.mediquitous.productpoc.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * 셀러 인덱스 서비스 구현체
 *
 * Go 서버의 seller/index_service.go 로직을 Kotlin으로 변환
 */
@Service
class SellerIndexServiceImpl(
    // TODO: OpenSearch 클라이언트 및 JPA Repository 주입
    // private val openSearchClient: OpenSearchClient,
    // private val sellerRepository: SellerRepository,
) : SellerIndexService {
    override fun updateSeller(sellerId: Long) {
        logger.info { "셀러 인덱스 업데이트: sellerId=$sellerId" }

        try {
            // 1. PostgreSQL에서 셀러 정보 조회
            // val seller = sellerRepository.findById(sellerId)
            //     .orElseThrow { EntityNotFoundException("Seller not found: $sellerId") }

            // 2. 셀러 정보를 OpenSearch 문서로 변환
            // val document = convertToDocument(seller)

            // 3. OpenSearch에 인덱싱
            // openSearchClient.index(...)

            logger.info { "셀러 인덱스 업데이트 완료: sellerId=$sellerId" }
        } catch (e: Exception) {
            logger.error(e) { "셀러 인덱스 업데이트 실패: sellerId=$sellerId" }
            throw e
        }
    }

    override fun deleteSeller(sellerId: Long) {
        logger.info { "셀러 인덱스 삭제: sellerId=$sellerId" }

        try {
            // OpenSearch에서 문서 삭제
            // openSearchClient.delete(...)

            logger.info { "셀러 인덱스 삭제 완료: sellerId=$sellerId" }
        } catch (e: Exception) {
            logger.error(e) { "셀러 인덱스 삭제 실패: sellerId=$sellerId" }
            throw e
        }
    }

    override fun bulkUpdateSellers(sellerIds: List<Long>) {
        logger.info { "셀러 일괄 인덱스 업데이트: ${sellerIds.size}개" }

        if (sellerIds.isEmpty()) {
            logger.debug { "업데이트할 셀러가 없음" }
            return
        }

        try {
            // 1. PostgreSQL에서 셀러 목록 조회
            // val sellers = sellerRepository.findAllById(sellerIds)

            // 2. OpenSearch Bulk API로 일괄 인덱싱
            // openSearchClient.bulk(...)

            logger.info { "셀러 일괄 인덱스 업데이트 완료: ${sellerIds.size}개" }
        } catch (e: Exception) {
            logger.error(e) { "셀러 일괄 인덱스 업데이트 실패: sellerIds=$sellerIds" }
            throw e
        }
    }
}
