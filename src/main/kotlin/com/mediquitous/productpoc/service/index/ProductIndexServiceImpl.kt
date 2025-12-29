package com.mediquitous.productpoc.service.index

import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.service.index.chain.ProductIndexChainAssembler
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch.core.DeleteRequest
import org.opensearch.client.opensearch.core.IndexRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * 상품 인덱스 서비스 구현체
 *
 * Go 서버의 product/index_service.go 로직을 Kotlin으로 변환
 */
@Service
class ProductIndexServiceImpl(
    private val chainAssembler: ProductIndexChainAssembler,
    private val openSearchClient: OpenSearchClient,
    private val objectMapper: ObjectMapper,
    @Value("\${opensearch.index.products.write:products}")
    private val writeIndex: String,
) : ProductIndexService {
    override fun updateProduct(productId: Long) {
        logger.info { "상품 인덱스 업데이트: productId=$productId" }

        try {
            // 1. 체인을 통해 상품 문서 조립
            val document =
                runBlocking {
                    chainAssembler.buildProductDocument(productId)
                }

            // 2. 문서가 null이면 삭제 대상
            if (document == null) {
                logger.info { "삭제 대상 상품: productId=$productId" }
                deleteProduct(productId)
                return
            }

            // 3. OpenSearch에 인덱싱
            val request =
                IndexRequest
                    .Builder<Any>()
                    .index(writeIndex)
                    .id(productId.toString())
                    .document(document)
                    .build()

            val response = openSearchClient.index(request)

            logger.info { "상품 인덱스 업데이트 완료: productId=$productId, result=${response.result()}" }
        } catch (e: Exception) {
            logger.error(e) { "상품 인덱스 업데이트 실패: productId=$productId" }
            throw e
        }
    }

    override fun deleteProduct(productId: Long) {
        logger.info { "상품 인덱스 삭제: productId=$productId" }

        try {
            val request =
                DeleteRequest
                    .Builder()
                    .index(writeIndex)
                    .id(productId.toString())
                    .build()

            val response = openSearchClient.delete(request)

            if (response.result().name == "NOT_FOUND") {
                logger.warn { "삭제할 상품이 인덱스에 없음: productId=$productId" }
            } else {
                logger.info { "상품 인덱스 삭제 완료: productId=$productId, result=${response.result()}" }
            }
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
            // 개별 업데이트 (추후 Bulk API로 최적화 가능)
            productIds.forEach { productId ->
                try {
                    updateProduct(productId)
                } catch (e: Exception) {
                    logger.error(e) { "상품 인덱스 업데이트 실패 (개별): productId=$productId" }
                    // 개별 실패 시 계속 진행
                }
            }

            logger.info { "상품 일괄 인덱스 업데이트 완료: ${productIds.size}개" }
        } catch (e: Exception) {
            logger.error(e) { "상품 일괄 인덱스 업데이트 실패: productIds=$productIds" }
            throw e
        }
    }
}
