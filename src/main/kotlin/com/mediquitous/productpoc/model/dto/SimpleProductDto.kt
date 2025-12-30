package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

/**
 * 간단한 상품 정보 DTO (클라이언트 응답용)
 *
 * Go 서버의 internal/service/dto/simple_product.go 변환
 *
 * Product.toSimple()을 통해 생성되며,
 * 목록 조회 등에서 클라이언트에게 반환하는 최종 형태
 */
@Schema(description = "간단한 상품 정보")
data class SimpleProductDto(
    val id: Long,
    val code: String? = null,
    val slug: String? = null,
    val title: String? = null,
    val name: String? = null,
    val price: Double = 0.0,
    @JsonProperty("discount_price")
    val discountPrice: Double = 0.0,
    @JsonProperty("discount_rate")
    val discountRate: Double = 0.0,
    val image: AttachmentDto? = null,
    val display: OffsetDateTime? = null,
    val selling: OffsetDateTime? = null,
    @JsonProperty("option_set")
    val optionSet: List<ProductOption> = emptyList(),
    @JsonProperty("english_name")
    val englishName: String? = null,
    @JsonProperty("leaf_categories")
    val leafCategories: List<CategoryDto> = emptyList(),
    val seller: String = "",
    @JsonProperty("seller_slug")
    val sellerSlug: String? = null,
    @JsonProperty("seller_status")
    val sellerStatus: String = "",
    @JsonProperty("seller_display")
    val sellerDisplay: Boolean = false,
    val brand: String? = null,
    @JsonProperty("shipping_fee_benefit")
    val shippingFeeBenefitDto: BenefitDto? = null,
    @JsonProperty("benefit_end")
    val benefitEnd: OffsetDateTime? = null,
    val express: Boolean = false,
    @JsonProperty("quantity_limit")
    val quantityLimit: Int? = null,
    @JsonProperty("icon_set")
    val iconSet: List<String> = emptyList(),
    @JsonProperty("is_original")
    val isOriginal: Boolean = false,
    @JsonProperty("review_count")
    val reviewCount: Int = 0,
    @JsonProperty("review_average")
    val reviewAverage: Double = 0.0,
    @JsonProperty("total_like_count")
    val totalLikeCount: Int = 0,
)
