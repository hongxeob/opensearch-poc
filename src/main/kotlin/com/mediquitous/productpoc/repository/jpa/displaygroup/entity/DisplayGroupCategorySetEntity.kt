package com.mediquitous.productpoc.repository.jpa.displaygroup.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 기획전-카테고리 매핑 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 기획전(DisplayGroup)과 카테고리(Category)의 다대다 관계 테이블
 * - 특정 기획전에 포함되는 카테고리들을 관리
 * - 카테고리별 기획전 필터링에 활용
 */
@Entity
@Table(name = "shopping_displaygroup_category_set")
@Immutable
data class DisplayGroupCategorySetEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 기획전 ID (FK to shopping_displaygroup)
    @Column(name = "displaygroup_id")
    val displayGroupId: Long? = null,
    // 카테고리 ID (FK to shopping_category)
    @Column(name = "category_id")
    val categoryId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DisplayGroupCategorySetEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "DisplayGroupCategorySetEntity(id=$id, displayGroupId=$displayGroupId, categoryId=$categoryId)"
}
