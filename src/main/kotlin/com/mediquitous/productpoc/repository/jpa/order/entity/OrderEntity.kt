package com.mediquitous.productpoc.repository.jpa.order.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 주문 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 고객의 주문 정보
 * - soft delete 지원 (deleted 컬럼)
 */
@Entity
@Table(name = "shopping_order")
@Immutable
data class OrderEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 주문번호 (사용자 노출용)
    @Column(name = "code", length = 200)
    val code: String? = null,
    // 고객 정보
    @Column(name = "customer_id")
    val customerId: Long? = null,
    @Column(name = "customer_group_id")
    val customerGroupId: Long? = null,
    @Column(name = "customer_name", length = 200)
    val customerName: String? = null,
    @Column(name = "customer_email", length = 254)
    val customerEmail: String? = null,
    @Column(name = "customer_phone", length = 200)
    val customerPhone: String? = null,
    // 수령인 정보
    @Column(name = "receiver_name", length = 200)
    val receiverName: String? = null,
    @Column(name = "receiver_name_en", length = 200)
    val receiverNameEn: String? = null,
    @Column(name = "receiver_phone", length = 200)
    val receiverPhone: String? = null,
    @Column(name = "receiver_address", columnDefinition = "jsonb")
    val receiverAddress: String? = null,
    @Column(name = "shipping_message")
    val shippingMessage: String? = null,
    // 금액 정보
    @Column(name = "currency", length = 10)
    val currency: String? = null,
    @Column(name = "exchange_rate", precision = 10, scale = 2)
    val exchangeRate: BigDecimal? = null,
    @Column(name = "order_amount", precision = 20, scale = 2)
    val orderAmount: BigDecimal? = null,
    @Column(name = "shipping_fee", precision = 20, scale = 2)
    val shippingFee: BigDecimal? = null,
    @Column(name = "shipping_fee_discount", precision = 20, scale = 2)
    val shippingFeeDiscount: BigDecimal? = null,
    @Column(name = "membership_discount", precision = 20, scale = 2)
    val membershipDiscount: BigDecimal? = null,
    @Column(name = "coupon_discount", precision = 20, scale = 2)
    val couponDiscount: BigDecimal? = null,
    @Column(name = "credits_spent", precision = 20, scale = 2)
    val creditsSpent: BigDecimal? = null,
    @Column(name = "points_spent", precision = 20, scale = 2)
    val pointsSpent: BigDecimal? = null,
    @Column(name = "billing_amount", precision = 20, scale = 2)
    val billingAmount: BigDecimal? = null,
    @Column(name = "payment_amount", precision = 20, scale = 2)
    val paymentAmount: BigDecimal? = null,
    @Column(name = "payment_fee", precision = 20, scale = 2)
    val paymentFee: BigDecimal? = null,
    @Column(name = "point_incentive", precision = 20, scale = 2)
    val pointIncentive: BigDecimal? = null,
    @Column(name = "supply_price", precision = 20, scale = 2)
    val supplyPrice: BigDecimal? = null,
    // 결제 정보
    @Column(name = "paid_status", length = 20)
    val paidStatus: String? = null,
    @Column(name = "transaction_id", length = 100)
    val transactionId: String? = null,
    @Column(name = "payment_gateway_name", length = 20)
    val paymentGatewayName: String? = null,
    // 상태 정보
    @Column(name = "shipping_status", length = 100)
    val shippingStatus: String? = null,
    @Column(name = "cancel_status", length = 20)
    val cancelStatus: String? = null,
    // 배송 정보
    @Column(name = "preferred_shipping_company_id")
    val preferredShippingCompanyId: Long? = null,
    // 기타
    @Column(name = "version", length = 100)
    val version: String? = null,
    @Column(name = "info", columnDefinition = "jsonb")
    val info: String? = null,
    @Column(name = "frozen_relations", columnDefinition = "jsonb")
    val frozenRelations: String? = null,
    @Column(name = "refund_as_credit")
    val refundAsCredit: Boolean? = null,
    // 날짜 정보
    @Column(name = "ordered")
    val ordered: OffsetDateTime? = null,
    @Column(name = "paid")
    val paid: OffsetDateTime? = null,
    @Column(name = "canceled")
    val canceled: OffsetDateTime? = null,
    @Column(name = "return_confirmed")
    val returnConfirmed: OffsetDateTime? = null,
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    @Column(name = "deleted")
    val deleted: OffsetDateTime? = null,
) {
    /**
     * 활성화 상태 여부
     */
    val isActive: Boolean
        get() = deleted == null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "OrderEntity(id=$id, code=$code, customerId=$customerId)"
}
