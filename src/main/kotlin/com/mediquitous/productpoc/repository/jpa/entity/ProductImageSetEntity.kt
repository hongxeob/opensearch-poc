package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 상품 이미지 세트 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 상품과 이미지(Attachment)의 다대다 관계 테이블
 */
@Entity
@Table(name = "shopping_product_image_set")
@Immutable
data class ProductImageSetEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 상품 ID (FK)
    @Column(name = "product_id")
    val productId: Long? = null,
    // 첨부파일 ID (FK to shopping_attachment)
    @Column(name = "attachment_id")
    val attachmentId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductImageSetEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "ProductImageSetEntity(id=$id, productId=$productId, attachmentId=$attachmentId)"
}
