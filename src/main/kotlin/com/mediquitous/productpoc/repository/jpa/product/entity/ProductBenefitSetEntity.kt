package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 상품 혜택 세트 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 상품과 혜택의 다대다 관계 테이블
 * - 상품별 할인 금액 포함
 */
@Entity
@Table(name = "shopping_benefitproduct")
@Immutable
data class ProductBenefitSetEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 혜택 ID (FK to shopping_benefit)
    @Column(name = "benefit_id")
    val benefitId: Long? = null,
    // 상품 ID (FK to shopping_product)
    @Column(name = "product_id")
    val productId: Long? = null,
    // 상품별 할인 금액
    @Column(name = "discount_amount", precision = 20, scale = 2)
    val discountAmount: BigDecimal? = null,
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
        if (other !is ProductBenefitSetEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "ProductBenefitSetEntity(id=$id, productId=$productId, benefitId=$benefitId)"
}
