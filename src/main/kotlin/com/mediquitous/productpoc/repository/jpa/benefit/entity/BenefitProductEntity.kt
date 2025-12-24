package com.mediquitous.productpoc.repository.jpa.benefit.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 혜택-상품 매핑 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 특정 상품에 적용되는 개별 혜택 정보
 * - Benefit과 Product의 다대다 관계 테이블
 * - soft delete 지원 (deleted 컬럼)
 */
@Entity
@Table(name = "shopping_benefitproduct")
@Immutable
data class BenefitProductEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 혜택 ID (FK to shopping_benefit)
    @Column(name = "benefit_id")
    val benefitId: Long? = null,
    // 상품 ID (FK to shopping_product)
    @Column(name = "product_id")
    val productId: Long? = null,
    // 할인 금액 (상품별 고정 할인액)
    @Column(name = "discount_amount", precision = 20, scale = 2)
    val discountAmount: BigDecimal? = null,
    // 날짜 정보
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    // soft delete 시각
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
        if (other !is BenefitProductEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "BenefitProductEntity(id=$id, benefitId=$benefitId, productId=$productId, discountAmount=$discountAmount)"
}
