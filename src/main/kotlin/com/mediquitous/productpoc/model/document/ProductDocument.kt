package com.mediquitous.productpoc.model.document

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * OpenSearch Product Document
 *
 * Go 서버의 dao/product.go 구조를 Kotlin으로 변환
 * OpenSearch에 색인되는 상품 문서 모델
 */
data class ProductDocument(
    val id: Long,
    val code: String? = null,
    @JsonProperty("custom_code")
    val customCode: String? = null,
    val slug: String? = null,
    val name: String? = null,
    @JsonProperty("english_name")
    val englishName: String? = null,
    @JsonProperty("internal_name")
    val internalName: String? = null,
    @JsonProperty("model_name")
    val modelName: String? = null,
    val description: String? = null,
    val title: String? = null,
    val label: List<String> = emptyList(),
    val express: Boolean = false,
    val annotation: String? = null,
    @JsonProperty("brand_id")
    val brandId: Long? = null,
    @JsonProperty("trend_id")
    val trendId: Long? = null,
    val image: AttachmentDocument? = null,
    val images: List<String> = emptyList(),
    @JsonProperty("guide_image")
    val guideImage: GuideImageDocument? = null,
    val info: Any? = null,
    @JsonProperty("size_info")
    val sizeInfo: String? = null,
    val price: Double = 0.0,
    val material: String? = null,
    @JsonProperty("cloth_fabric")
    val clothFabric: String? = null,
    val weight: Double? = null,
    val season: String? = null,
    @JsonProperty("origin_id")
    val originId: Long? = null,
    @JsonProperty("manufacturer_id")
    val manufacturerId: Long? = null,
    @JsonProperty("option_type")
    val optionType: String = "",
    @JsonProperty("member_only")
    val memberOnly: Boolean = false,
    @JsonProperty("quantity_limit_type")
    val quantityLimitType: String = "",
    @JsonProperty("quantity_limit")
    val quantityLimit: Int? = null,
    val repurchasable: Boolean = false,
    val display: Instant? = null,
    val selling: Instant? = null,
    @JsonProperty("is_selling")
    val isSelling: Boolean = false,
    val released: Instant? = null,
    val deleted: Instant? = null,
    @JsonProperty("best_order")
    val bestOrder: BestOrderDocument? = null,
    val seller: SellerDocument? = null,
    val options: List<OptionDocument> = emptyList(),
    val variants: List<VariantDocument> = emptyList(),
    val stock: List<StockDocument> = emptyList(),
    @JsonProperty("is_original")
    val isOriginal: Boolean = false,
    @JsonProperty("has_shoe_category")
    val hasShoeCategory: Boolean = false,
    val categories: List<CategoryDocument> = emptyList(),
    @JsonProperty("display_group")
    val displayGroup: List<DisplayGroupDocument> = emptyList(),
    @JsonProperty("related_product_ids")
    val relatedProductIds: List<Long> = emptyList(),
)

/**
 * 첨부파일 문서
 */
data class AttachmentDocument(
    val id: Long,
    @JsonProperty("mime_type")
    val mimeType: String? = null,
    val file: String? = null,
    val seq: Int? = null,
)

/**
 * 가이드 이미지 문서
 */
data class GuideImageDocument(
    val id: Long,
    val name: String? = null,
    val image: AttachmentDocument? = null,
)

/**
 * 베스트 오더 문서
 */
data class BestOrderDocument(
    @JsonProperty("order_count")
    val orderCount: Int, // 주문 수
    @JsonProperty("like_count")
    val likeCount: Int, // 좋아요 수
    @JsonProperty("cart_count")
    val cartCount: Int, // 카트담기 수
    @JsonProperty("view_count")
    val viewCount: Int, // 상세 진입 수
    @JsonProperty("review_average")
    val reviewAverage: Double?, // 리뷰 평점
    @JsonProperty("review_count")
    val reviewCount: Int, // 리뷰 수
    @JsonProperty("total_like_count")
    val totalLikeCount: Int, // 총 좋아요 수
    @JsonProperty("sales_amount")
    val salesAmount: Int, // 판매 금액
    @JsonProperty("discounted_price")
    val discountedPrice: Double, // 할인 금액
)

/**
 * 옵션 문서
 */
data class OptionDocument(
    val id: Long,
    val name: String? = null,
    val value: String? = null,
    val hexcode: String? = null,
    @JsonProperty("search_name")
    val searchName: String? = null,
    val model: Boolean? = null,
    @JsonProperty("name_seq")
    val nameSeq: Int? = null,
    @JsonProperty("value_seq")
    val valueSeq: Int? = null,
)

/**
 * 품목(Variant) 문서
 */
data class VariantDocument(
    val id: Long,
    val code: String? = null,
    @JsonProperty("use_inventory")
    val useInventory: Boolean = false,
    @JsonProperty("display_soldout")
    val displaySoldout: Boolean = false,
    @JsonProperty("inventory_type")
    val inventoryType: String? = null,
    @JsonProperty("quantity_check_type")
    val quantityCheckType: String? = null,
    val quantity: Int = 0,
    @JsonProperty("safety_quantity")
    val safetyQuantity: Int = 0,
    val barcode: String? = null,
    val barcode2: String? = null,
    @JsonProperty("external_barcode")
    val externalBarcode: String? = null,
    val deleted: Instant? = null,
    @JsonProperty("option_ids")
    val optionIds: List<Long> = emptyList(),
    val options: Map<String, Any>? = null,
    @JsonProperty("additional_price")
    val additionalPrice: Double = 0.0,
    val price: Double = 0.0,
    val display: Instant? = null,
    val selling: Instant? = null,
    @JsonProperty("sold_out")
    val soldOut: Boolean = false,
    val express: Boolean = false,
    @JsonProperty("available_stock_quantities")
    val availableStockQuantities: Int = 0,
)

/**
 * 재고 문서
 */
data class StockDocument(
    val id: Long,
    @JsonProperty("product_variant_id")
    val productVariantId: Long,
    val quantity: Int = 0,
    @JsonProperty("warehouse_id")
    val warehouseId: Long? = null,
    @JsonProperty("warehouse_name")
    val warehouseName: String? = null,
    @JsonProperty("retail_store_name")
    val retailStoreName: String? = null,
    @JsonProperty("is_quick_delivery")
    val isQuickDelivery: Boolean = false,
)

/**
 * 카테고리 문서
 */
data class CategoryDocument(
    val id: Long,
    @JsonProperty("parent_id")
    val parentId: Long? = null,
    val name: String? = null,
    @JsonProperty("display_name")
    val displayName: String? = null,
    val slug: String? = null,
    @JsonProperty("is_visible")
    val isVisible: Boolean = true,
    @JsonProperty("is_leaf")
    val isLeaf: Boolean = false,
)

/**
 * 기획전 문서
 */
data class DisplayGroupDocument(
    val id: Long,
    @JsonProperty("product_seq")
    val productSeq: Int = 0,
)
