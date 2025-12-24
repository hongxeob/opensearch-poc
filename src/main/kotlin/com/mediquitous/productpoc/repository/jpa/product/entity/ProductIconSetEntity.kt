package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 상품 아이콘 세트 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 상품에 표시되는 아이콘(뱃지) 정보
 */
@Entity
@Table(name = "shopping_product_icon_set")
@Immutable
data class ProductIconSetEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 상품 ID (FK)
    @Column(name = "product_id")
    val productId: Long? = null,
    // 아이콘 ID (FK to shopping_icon)
    @Column(name = "icon_id")
    val iconId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductIconSetEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "ProductIconSetEntity(id=$id, productId=$productId, iconId=$iconId)"
}
