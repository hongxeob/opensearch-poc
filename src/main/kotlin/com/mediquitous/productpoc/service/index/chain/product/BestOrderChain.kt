package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.model.document.BestOrderDocument
import com.mediquitous.productpoc.repository.jpa.product.ProductBestOrderJpaRepository
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
 * 베스트 오더 체인
 *
 * Go 서버의 best_order_chain.go 로직을 Kotlin으로 변환
 */
@Component
class BestOrderChain(
    private val bestOrderRepository: ProductBestOrderJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        logger.debug { "BestOrderChain 처리 시작: productId=$productId" }

        // Go의 goroutine + WaitGroup 패턴을 코루틴으로 재현
        return runBlocking {
            // DB 조회를 비동기로 시작
            val bestOrderDeferred =
                async(Dispatchers.IO) {
                    bestOrderRepository.findByProductId(productId)
                }

            // 다음 체인 실행 (병렬)
            val nextResult = next(context)

            // DB 조회 결과 대기
            val bestOrder = bestOrderDeferred.await()

            if (bestOrder != null) {
                builder.bestOrder =
                    BestOrderDocument(
                        orderCount = bestOrder.orderCount ?: 0,
                        likeCount = bestOrder.likeCount ?: 0,
                        cartCount = bestOrder.cartCount ?: 0,
                        viewCount = bestOrder.viewCount ?: 0,
                        reviewAverage = bestOrder.reviewAverage?.toDouble(),
                        reviewCount = bestOrder.reviewCount ?: 0,
                        totalLikeCount = bestOrder.totalLikeCount ?: 0,
                        salesAmount = bestOrder.salesAmount ?: 0,
                        discountedPrice = bestOrder.discountedPrice?.toDouble() ?: 0.0,
                    )
            }

            logger.debug { "BestOrderChain 처리 완료: productId=$productId" }
            nextResult
        }
    }
}
