package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 상품-카테고리 관계 엔티티 (읽기 전용 DAO)
 *
 * ManyToMany 조인 테이블
 */
@Entity
@Table(name = "shopping_product_category_set")
@Immutable
data class ProductCategorySetEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "product_id")
    val productId: Long? = null,
    @Column(name = "category_id")
    val categoryId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductCategorySetEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
