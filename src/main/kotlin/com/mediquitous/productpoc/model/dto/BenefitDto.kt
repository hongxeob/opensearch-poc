package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

/**
 * 혜택(할인) 정보 DTO
 *
 * Go 서버의 internal/service/dto/benefit.go 변환
 */
data class BenefitDto(
    val id: Long,
    val seq: Int? = null,
    @JsonProperty("is_pinned")
    val isPinned: Boolean? = null,
    val name: String,
    @JsonProperty("internal_name")
    val internalName: String? = null,
    val activated: Boolean,
    val deleted: OffsetDateTime? = null,
    val type: String,
    val begin: OffsetDateTime,
    val end: OffsetDateTime,
    @JsonProperty("minimum_order_amount")
    val minimumOrderAmount: Double? = null,
    @JsonProperty("detail_setting")
    val detailSetting: Map<String, Any>? = null,
    @JsonProperty("discount_value")
    val discountValue: Double = 0.0,
    @JsonProperty("is_fixed_discount_by_product")
    val isFixedDiscountByProduct: Boolean = false,
    @JsonProperty("is_collaboration")
    val isCollaboration: Boolean = false,
    @JsonIgnore
    val excludeDisplayGroupIds: List<Long> = emptyList(),
) {
    companion object {
        // 혜택 타입 상수
        const val TYPE_PERIOD = "discount-period" // 기간 할인
        const val TYPE_REPURCHASE = "discount-repurchase" // 재구매 할인 (사용하지 않음)
        const val TYPE_BULK = "discount-bulk" // 대량구매할인 (사용하지 않음)
        const val TYPE_MEMBER = "discount-member" // 회원 할인 (사용하지 않음)
        const val TYPE_NEW_PRODUCT = "discount-new-product" // 새상품 할인 (사용하지 않음)
        const val TYPE_DELIVERY = "discount-delivery" // 배송비 할인
        const val TYPE_PRESENT = "present" // 사은품
        const val TYPE_PRESENT_BUNDLE = "present-bundle" // 1+n 이벤트 (사용하지 않음)
    }
}
