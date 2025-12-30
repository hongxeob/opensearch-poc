package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

/**
 * 상품 품목(Variant) DTO
 *
 * Go 서버의 internal/service/dto/variant.go 변환
 */
data class ProductVariant(
    val id: Long,
    val express: Boolean = false,
    @JsonProperty("available_stock_quantities")
    val availableStockQuantities: Int = 0,
    val price: Double = 0.0,
    @JsonProperty("discount_price")
    var discountPrice: Double = 0.0,
    @JsonProperty("sold_out")
    val soldOut: Boolean = false,
    @JsonProperty("option_set")
    val optionSet: List<ProductOption> = emptyList(),
    @JsonProperty("option_values")
    val optionValues: String? = null,
    val code: String? = null,
    val display: OffsetDateTime? = null,
    val selling: OffsetDateTime? = null,
    val options: Map<String, Any>? = null,
    @JsonProperty("additional_price")
    val additionalPrice: Double = 0.0,
    @JsonProperty("use_inventory")
    val useInventory: Boolean = false,
    val quantity: Int = 0,
    @JsonProperty("safety_quantity")
    val safetyQuantity: Int = 0,
    val deleted: OffsetDateTime? = null,
    val barcode: String? = null,
    @JsonProperty("external_barcode")
    val externalBarcode: String? = null,
    val barcode2: String? = null,
    @JsonProperty("display_soldout")
    val displaySoldout: Boolean = false,
    @JsonProperty("inventory_type")
    val inventoryType: String? = null,
    @JsonProperty("quantity_check_type")
    val quantityCheckType: String? = null,
)

/**
 * 재고 DTO
 *
 * Go 서버의 internal/service/dto/stock.go 변환
 */
data class Stock(
    val id: Long,
    @JsonProperty("product_variant_id")
    val productVariantId: Long,
    val quantity: Int = 0,
    @JsonProperty("safety_quantity")
    val safetyQuantity: Int = 0,
    @JsonProperty("sold_out")
    val soldOut: Boolean = false,
)
