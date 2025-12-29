package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.repository.jpa.product.ProductRelatedProductsJpaRepository
import com.mediquitous.productpoc.service.index.chain.BaseIndexChain
import com.mediquitous.productpoc.service.index.chain.IndexContext
import com.mediquitous.productpoc.service.index.chain.ProductDocumentBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 연관 상품 체인
 *
 * Go 서버의 related_product_chain.go 로직을 Kotlin으로 변환
 */
@Component
class RelatedProductChain(
    private val relatedProductsRepository: ProductRelatedProductsJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        logger.debug { "RelatedProductChain 처리 시작: productId=$productId" }

        return runBlocking {
            // DB 조회를 비동기로 시작
            val relatedProductsDeferred =
                async(Dispatchers.IO) {
                    relatedProductsRepository.findByFromProductId(productId)
                }

            // 다음 체인 실행 (병렬)
            val nextResult = next(context)

            // DB 조회 결과 대기
            val relatedProducts = relatedProductsDeferred.await()

            relatedProducts.forEach { rp ->
                rp.toProductId?.let { builder.relatedProductIds.add(it) }
            }

            logger.debug { "RelatedProductChain 처리 완료: productId=$productId, relatedCount=${builder.relatedProductIds.size}" }
            nextResult
        }
    }
}
