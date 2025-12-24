package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 혜택 엔티티 (읽기 전용 DAO)
 *
 * - ProductBenefitSet에서 benefit_id로 참조
 * - 상품 할인/혜택 정보
 */
@Entity
@Table(name = "shopping_benefit")
@Immutable
data class BenefitEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 혜택명
    @Column(name = "name", length = 255)
    val name: String? = null,
    // 내부 관리명
    @Column(name = "internal_name", length = 255)
    val internalName: String? = null,
    // 혜택 타입
    @Column(name = "type", length = 50)
    val type: String? = null,
    // 할인값/할인율
    @Column(name = "discount_value", precision = 20, scale = 2)
    val discountValue: BigDecimal? = null,
    // 최소 주문 금액
    @Column(name = "minimum_order_amount", precision = 20, scale = 2)
    val minimumOrderAmount: BigDecimal? = null,
    // 활성화 여부
    @Column(name = "activated")
    val activated: Boolean? = null,
    // 쿠폰 사용 가능 여부
    @Column(name = "can_use_coupon")
    val canUseWithCoupon: Boolean? = null,
    // 고정 여부
    @Column(name = "is_pinned")
    val isPinned: Boolean? = null,
    // 1P 적용 여부
    @Column(name = "apply_1P")
    val apply1P: Boolean? = null,
    // 상품별 고정 할인 여부
    @Column(name = "is_fixed_discount_by_product")
    val isFixedDiscountByProduct: Boolean? = null,
    // 협업 혜택 여부
    @Column(name = "is_collaboration")
    val isCollaboration: Boolean? = null,
    // 누구 비율
    @Column(name = "nugu_rate", precision = 10)
    val nuguRate: BigDecimal? = null,
    // 수수료율
    @Column(name = "commission_rate", precision = 10)
    val commissionRate: BigDecimal? = null,
    // 협업 수수료율 (JSONB)
    @Column(name = "collaboration_commission_rates", columnDefinition = "jsonb")
    val collaborationCommissionRates: String? = null,
    // 상세 설정 (JSONB)
    @Column(name = "detail_setting", columnDefinition = "jsonb")
    val detailSetting: String? = null,
    // 혜택 시작일
    @Column(name = "begin")
    val begin: OffsetDateTime? = null,
    // 혜택 종료일
    @Column(name = "end")
    val end: OffsetDateTime? = null,
    // 정렬 순서
    @Column(name = "seq")
    val seq: Int? = null,
    // 아이콘 ID
    @Column(name = "icon_id")
    val iconId: Long? = null,
    // 기획전 ID
    @Column(name = "displaygroup_id")
    val displayGroupId: Long? = null,
    // 날짜 정보
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    @Column(name = "deleted")
    val deleted: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BenefitEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "BenefitEntity(id=$id, name=$name, type=$type)"
}
