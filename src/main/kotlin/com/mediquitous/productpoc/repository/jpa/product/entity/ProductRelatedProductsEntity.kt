package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 연관 상품 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 상품 간 연관관계를 나타내는 테이블
 */
@Entity
@Table(name = "shopping_product_related_products")
@Immutable
data class ProductRelatedProductsEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 기준 상품 ID (FK to shopping_product)
    @Column(name = "from_product_id")
    val fromProductId: Long? = null,
    // 연관 상품 ID (FK to shopping_product)
    @Column(name = "to_product_id")
    val toProductId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductRelatedProductsEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "ProductRelatedProductsEntity(id=$id, fromProductId=$fromProductId, toProductId=$toProductId)"
}
