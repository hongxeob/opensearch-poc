package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 주문 품목 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 주문에 포함된 개별 상품 품목 정보
 * - 배송, 클레임 등의 상태 관리
 */
@Entity
@Table(name = "shopping_orderitem")
@Immutable
data class OrderItemEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 주문 품목 코드
    @Column(name = "code", length = 100)
    val code: String? = null,
    // 연관 관계
    @Column(name = "order_id")
    val orderId: Long? = null,
    @Column(name = "product_variant_id")
    val productVariantId: Long? = null,
    @Column(name = "seller_id")
    val sellerId: Long? = null,
    @Column(name = "supplier_id")
    val supplierId: Long? = null,
    @Column(name = "shipping_id")
    val shippingId: Long? = null,
    @Column(name = "promotion_id")
    val promotionId: Long? = null,
    @Column(name = "warehouse_id")
    val warehouseId: Long? = null,
    // 수량 및 가격
    @Column(name = "quantity")
    val quantity: Int? = null,
    @Column(name = "price", precision = 20, scale = 2)
    val price: BigDecimal? = null,
    @Column(name = "additional_price", precision = 20, scale = 2)
    val additionalPrice: BigDecimal? = null,
    @Column(name = "additional_discount", precision = 20, scale = 2)
    val additionalDiscount: BigDecimal? = null,
    @Column(name = "membership_discount", precision = 20, scale = 2)
    val membershipDiscount: BigDecimal? = null,
    @Column(name = "coupon_discount", precision = 20, scale = 2)
    val couponDiscount: BigDecimal? = null,
    @Column(name = "discounted_amount", precision = 20, scale = 2)
    val discountedAmount: BigDecimal? = null,
    @Column(name = "supply_price", precision = 20, scale = 2)
    val supplyPrice: BigDecimal? = null,
    // 정산 정보
    @Column(name = "commission", precision = 10)
    val commission: BigDecimal? = null,
    @Column(name = "commission_rate", precision = 10)
    val commissionRate: BigDecimal? = null,
    @Column(name = "nugu_ratio", precision = 10)
    val nuguRatio: BigDecimal? = null,
    @Column(name = "commission_currency", length = 10)
    val commissionCurrency: String? = null,
    @Column(name = "base_currency_price", precision = 20, scale = 2)
    val baseCurrencyPrice: BigDecimal? = null,
    @Column(name = "base_currency_amount", precision = 20, scale = 2)
    val baseCurrencyAmount: BigDecimal? = null,
    @Column(name = "shipping_cost", precision = 20, scale = 2)
    val shippingCost: BigDecimal? = null,
    // 상태 정보
    @Column(name = "status", length = 100)
    val status: String? = null,
    @Column(name = "claim_status", length = 100)
    val claimStatus: String? = null,
    @Column(name = "shipping_fee_type", length = 100)
    val shippingFeeType: String? = null,
    // 플래그
    @Column(name = "one_plus_n")
    val onePlusN: Boolean? = null,
    @Column(name = "gift")
    val gift: Boolean? = null,
    // 메모
    @Column(name = "memo")
    val memo: String? = null,
    @Column(name = "frozen_relations", columnDefinition = "jsonb")
    val frozenRelations: String? = null,
    // 날짜 정보 (주문-배송-클레임 흐름)
    @Column(name = "ordered")
    val ordered: OffsetDateTime? = null,
    @Column(name = "shipped")
    val shipped: OffsetDateTime? = null,
    @Column(name = "delivered")
    val delivered: OffsetDateTime? = null,
    @Column(name = "shipping_prepared")
    val shippingPrepared: OffsetDateTime? = null,
    // 클레임 날짜
    @Column(name = "cancel_requested")
    val cancelRequested: OffsetDateTime? = null,
    @Column(name = "canceled")
    val canceled: OffsetDateTime? = null,
    @Column(name = "return_requested")
    val returnRequested: OffsetDateTime? = null,
    @Column(name = "return_confirmed")
    val returnConfirmed: OffsetDateTime? = null,
    @Column(name = "return_collected")
    val returnCollected: OffsetDateTime? = null,
    @Column(name = "refunded")
    val refunded: OffsetDateTime? = null,
    @Column(name = "exchange_requested")
    val exchangeRequested: OffsetDateTime? = null,
    @Column(name = "exchanged")
    val exchanged: OffsetDateTime? = null,
    @Column(name = "exchange_collected")
    val exchangeCollected: OffsetDateTime? = null,
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItemEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "OrderItemEntity(id=$id, code=$code, orderId=$orderId, status=$status)"
}
