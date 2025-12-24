package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 커서 기반 페이지네이션 응답
 */
@Schema(description = "커서 기반 페이지네이션 응답")
data class CursorPaginationResponse<T>(
    @Schema(description = "전체 결과 개수", example = "100")
    val count: Long,
    @Schema(description = "결과 목록")
    val results: List<T>,
    @Schema(description = "다음 페이지 커서")
    @JsonProperty("next")
    val nextCursor: String?,
    @Schema(description = "이전 페이지 커서")
    @JsonProperty("previous")
    val previousCursor: String?,
) {
    companion object {
        fun <T> empty() =
            CursorPaginationResponse<T>(
                count = 0,
                results = emptyList(),
                nextCursor = null,
                previousCursor = null,
            )
    }
}

/**
 * 간단한 상품 정보 (목록 조회용)
 */
@Schema(description = "간단한 상품 정보")
data class SimpleProductDto(
    @Schema(description = "상품 ID", example = "1")
    val id: Long,
    @Schema(description = "상품 코드", example = "PROD001")
    val code: String,
    @Schema(description = "상품명", example = "청바지")
    val name: String,
    @Schema(description = "셀러 ID", example = "1")
    @JsonProperty("seller_id")
    val sellerId: Long,
    @Schema(description = "셀러명", example = "무신사")
    @JsonProperty("seller_name")
    val sellerName: String,
    @Schema(description = "셀러 슬러그", example = "musinsa")
    @JsonProperty("seller_slug")
    val sellerSlug: String,
    @Schema(description = "가격", example = "50000")
    val price: Long,
    @Schema(description = "할인 가격", example = "40000")
    @JsonProperty("discount_price")
    val discountPrice: Long?,
    @Schema(description = "할인율", example = "20")
    @JsonProperty("discount_rate")
    val discountRate: Int?,
    @Schema(description = "재고 여부", example = "true")
    @JsonProperty("in_stock")
    val inStock: Boolean,
    @Schema(description = "썸네일 URL")
    @JsonProperty("thumbnail_url")
    val thumbnailUrl: String?,
    @Schema(description = "좋아요 개수", example = "123")
    @JsonProperty("like_count")
    val likeCount: Long,
    @Schema(description = "리뷰 개수", example = "45")
    @JsonProperty("review_count")
    val reviewCount: Long,
    @Schema(description = "리뷰 평점", example = "4.5")
    @JsonProperty("review_score")
    val reviewScore: Double?,
)
