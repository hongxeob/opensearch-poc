package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 상품 품목 옵션 세트 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - ProductVariant와 Option의 다대다 관계 테이블
 * - 특정 품목이 어떤 옵션 조합으로 구성되는지 표현
 */
@Entity
@Table(name = "shopping_productvariant_option_set")
@Immutable
data class ProductVariantOptionSetEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 품목 ID (FK to shopping_productvariant)
    @Column(name = "productvariant_id")
    val productVariantId: Long? = null,
    // 옵션 ID (FK to shopping_option)
    @Column(name = "option_id")
    val optionId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductVariantOptionSetEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "ProductVariantOptionSetEntity(id=$id, productVariantId=$productVariantId, optionId=$optionId)"
}
