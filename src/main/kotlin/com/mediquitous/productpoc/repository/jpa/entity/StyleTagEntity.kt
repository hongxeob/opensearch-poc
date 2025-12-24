package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 스타일 태그 엔티티 (읽기 전용 DAO)
 *
 * - Seller와 연결되는 스타일 태그 정보
 */
@Entity
@Table(name = "nugustyling_styletag")
@Immutable
data class StyleTagEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 태그명
    @Column(name = "name", length = 200)
    val name: String? = null,
    // 일본어 태그명
    @Column(name = "name_jp", length = 200)
    val nameJp: String? = null,
    // 검색 키워드
    @Column(name = "keywords", length = 500)
    val keywords: String? = null,
    // 날짜 정보
    @Column(name = "created_at")
    val createdAt: OffsetDateTime? = null,
    @Column(name = "updated_at")
    val updatedAt: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StyleTagEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "StyleTagEntity(id=$id, name=$name, nameJp=$nameJp)"
}
