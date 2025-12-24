package com.mediquitous.productpoc.model.dto

import java.time.OffsetDateTime

/**
 * 최근 조회한 상품 정보 DTO
 *
 * @property productId 상품 ID
 * @property lastViewed 마지막 조회 시각
 */
data class RecentlyViewedProductDto(
    val productId: Long,
    val lastViewed: OffsetDateTime,
)
