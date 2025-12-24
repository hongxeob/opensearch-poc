package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 브랜드 엔티티 (읽기 전용 DAO)
 *
 * - Product에서 brand_id로 참조
 * - 브랜드 기본 정보
 */
@Entity
@Table(name = "shopping_brand")
@Immutable
data class BrandEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 브랜드명
    @Column(name = "name", length = 255)
    val name: String? = null,
    // 브랜드 코드
    @Column(name = "code", length = 100)
    val code: String? = null,
    // 활성화 여부
    @Column(name = "activated")
    val activated: Boolean? = null,
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
        if (other !is BrandEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "BrandEntity(id=$id, code=$code, name=$name)"
}
