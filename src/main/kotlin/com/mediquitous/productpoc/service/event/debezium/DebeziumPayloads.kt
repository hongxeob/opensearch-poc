package com.mediquitous.productpoc.service.event.debezium

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Debezium CDC 페이로드 모델들
 *
 * Go 서버의 internal/event/handler/debezium/topics.go 의 구조체 변환
 * 각 테이블의 CDC 이벤트에서 필요한 필드만 정의
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductPayload(
    val id: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductIconSetPayload(
    @JsonProperty("product_id")
    val productId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductImageSetPayload(
    @JsonProperty("product_id")
    val productId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductGuideImagePayload(
    val id: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductVariantPayload(
    @JsonProperty("product_id")
    val productId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductVariantOptionSetPayload(
    @JsonProperty("productvariant_id")
    val variantId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionPayload(
    @JsonProperty("product_id")
    val productId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductCategorySetPayload(
    @JsonProperty("product_id")
    val productId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DisplayGroupProductPayload(
    @JsonProperty("product_id")
    val productId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductBenefitSetPayload(
    @JsonProperty("product_id")
    val productId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductRelatedProductsPayload(
    @JsonProperty("from_product_id")
    val fromProductId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StockPayload(
    @JsonProperty("product_variant_id")
    val variantId: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductBestOrderPayload(
    @JsonProperty("product_id")
    val productId: Long,
)

// =====================================================
// Category 관련
// =====================================================

@JsonIgnoreProperties(ignoreUnknown = true)
data class CategoryPayload(
    val id: Long,
)

// =====================================================
// Seller 관련
// =====================================================

@JsonIgnoreProperties(ignoreUnknown = true)
data class SellerPayload(
    val id: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SellerStatPayload(
    @JsonProperty("seller_id")
    val sellerId: Long,
)

// =====================================================
// Like 관련
// =====================================================

@JsonIgnoreProperties(ignoreUnknown = true)
data class LikePayload(
    val id: Long,
    @JsonProperty("product_id")
    val productId: Long?,
    @JsonProperty("seller_id")
    val sellerId: Long?,
)
