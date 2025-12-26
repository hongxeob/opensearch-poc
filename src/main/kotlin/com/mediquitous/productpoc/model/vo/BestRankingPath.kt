package com.mediquitous.productpoc.model.vo

data class BestRankingPath(
    val path: String,
    val period: Int,
)

/**
 * 랭킹 상품 정보
 */
data class RankedProduct(
    val productId: Long,
    val rank: Int,
)
