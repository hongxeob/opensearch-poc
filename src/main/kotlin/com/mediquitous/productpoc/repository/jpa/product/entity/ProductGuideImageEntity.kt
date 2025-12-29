package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 상품 가이드 이미지 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 상품 상세 페이지 상단에 표시되는 가이드 이미지
 */
@Entity
@Table(name = "shopping_productguideimage")
@Immutable
data class ProductGuideImageEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 표시명 (어드민 내 이미지 대신 표시될 이름)
    @Column(name = "name", length = 200)
    val name: String? = null,
    // 가이드 이미지 ID (shopping_attachment FK)
    @Column(name = "image_id")
    val imageId: Long? = null,
    // 날짜 정보
    @Column(name = "created", nullable = false)
    val created: OffsetDateTime? = null,
    @Column(name = "updated", nullable = false)
    val updated: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductGuideImageEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "ProductGuideImageEntity(id=$id, name=$name, imageId=$imageId)"
}
