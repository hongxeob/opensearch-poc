package com.mediquitous.productpoc.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * 상품 인덱스 서비스 구현체
 *
 * Go 서버의 product/index_service.go 로직을 Kotlin으로 변환
 */
@Service
class ProductIndexServiceImpl(
    // TODO: OpenSearch 클라이언트 및 JPA Repository 주입
    // private val openSearchClient: OpenSearchClient,
    // private val productRepository: ProductRepository,
) : ProductIndexService {
    override fun updateProduct(productId: Long) {
        logger.info { "상품 인덱스 업데이트: productId=$productId" }

        try {
            // 1. PostgreSQL에서 상품 정보 조회
            // val product = productRepository.findById(productId)
            //     .orElseThrow { EntityNotFoundException("Product not found: $productId") }

            // 2. 상품 정보를 OpenSearch 문서로 변환
            // val document = convertToDocument(product)

            // 3. OpenSearch에 인덱싱
            // openSearchClient.index(...)

            logger.info { "상품 인덱스 업데이트 완료: productId=$productId" }
        } catch (e: Exception) {
            logger.error(e) { "상품 인덱스 업데이트 실패: productId=$productId" }
            throw e
        }
    }

    override fun deleteProduct(productId: Long) {
        logger.info { "상품 인덱스 삭제: productId=$productId" }

        try {
            // OpenSearch에서 문서 삭제
            // openSearchClient.delete(...)

            logger.info { "상품 인덱스 삭제 완료: productId=$productId" }
        } catch (e: Exception) {
            logger.error(e) { "상품 인덱스 삭제 실패: productId=$productId" }
            throw e
        }
    }

    override fun bulkUpdateProducts(productIds: List<Long>) {
        logger.info { "상품 일괄 인덱스 업데이트: ${productIds.size}개" }

        if (productIds.isEmpty()) {
            logger.debug { "업데이트할 상품이 없음" }
            return
        }

        try {
            // 1. PostgreSQL에서 상품 목록 조회
            // val products = productRepository.findAllById(productIds)

            // 2. OpenSearch Bulk API로 일괄 인덱싱
            // openSearchClient.bulk(...)

            logger.info { "상품 일괄 인덱스 업데이트 완료: ${productIds.size}개" }
        } catch (e: Exception) {
            logger.error(e) { "상품 일괄 인덱스 업데이트 실패: productIds=$productIds" }
            throw e
        }
    }
}
