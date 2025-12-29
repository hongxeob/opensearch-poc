package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.model.document.StockDocument
import com.mediquitous.productpoc.repository.jpa.product.StockJpaRepository
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
 * 재고 체인
 *
 * Go 서버의 stock_chain.go 로직을 Kotlin으로 변환
 */
@Component
class StockChain(
    private val stockRepository: StockJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        if (builder.variants.isEmpty()) {
            return next(context)
        }

        logger.debug { "StockChain 처리 시작: productId=$productId" }

        val variantIds = builder.variants.map { it.id }

        return runBlocking {
            // DB 조회를 비동기로 시작
            val stocksDeferred =
                async(Dispatchers.IO) {
                    stockRepository.findByProductVariantIds(variantIds)
                }

            // 다음 체인 실행 (병렬)
            val nextResult = next(context)

            // DB 조회 결과 대기
            val stocks = stocksDeferred.await()

            // 품목별 매핑
            val variantMap = builder.variants.associateBy { it.id }.toMutableMap()

            stocks.forEach { stock ->
                if (stock.quick && stock.quantity > 0) {
                    builder.express = true

                    variantMap[stock.productVariantId]?.let { variant ->
                        variantMap[stock.productVariantId] =
                            variant.copy(
                                express = true,
                                soldOut = variant.soldOut && stock.quantity <= 0,
                                availableStockQuantities = variant.availableStockQuantities + stock.quantity,
                            )
                    }
                }

                builder.stock.add(
                    StockDocument(
                        id = stock.id,
                        productVariantId = stock.productVariantId,
                        quantity = stock.quantity,
                        warehouseId = stock.warehouseId,
                        warehouseName = stock.warehouseName,
                        retailStoreName = stock.retailStoreName,
                        isQuickDelivery = stock.quick,
                    ),
                )
            }

            // 업데이트된 품목 목록으로 교체
            builder.variants.clear()
            builder.variants.addAll(variantMap.values)

            logger.debug { "StockChain 처리 완료: productId=$productId, stockCount=${builder.stock.size}" }
            nextResult
        }
    }
}
