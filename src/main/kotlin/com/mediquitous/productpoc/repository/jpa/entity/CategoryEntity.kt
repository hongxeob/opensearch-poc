package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 카테고리 엔티티 (읽기 전용 DAO)
 */
@Entity
@Table(name = "shopping_category")
@Immutable
data class CategoryEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "name", length = 255)
    val name: String? = null,
    @Column(name = "display_name", length = 255)
    val displayName: String? = null,
    @Column(name = "slug", length = 255)
    val slug: String? = null,
    @Column(name = "path", length = 255)
    val path: String? = null,
    @Column(name = "code", length = 5)
    val code: String? = null,
    @Column(name = "seq")
    val seq: Int? = null,
    @Column(name = "is_visible")
    val isVisible: Boolean? = null,
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    // 연관 ID
    @Column(name = "parent_id")
    val parentId: Long? = null,
    @Column(name = "icon_id")
    val iconId: Long? = null,
    @Column(name = "clearance_category_id")
    val clearanceCategoryId: Long? = null,
    @Column(name = "hscode_keyword_id")
    val hscodeKeywordId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CategoryEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "CategoryEntity(id=$id, name=$name, slug=$slug)"
}
