package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 아이콘 엔티티 (읽기 전용 DAO)
 *
 * - ProductIconSet에서 icon_id로 참조
 * - 상품에 표시되는 뱃지/라벨 정보
 */
@Entity
@Table(name = "shopping_icon")
@Immutable
data class IconEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 아이콘명
    @Column(name = "name", length = 100)
    val name: String? = null,
    // 정렬 순서
    @Column(name = "seq")
    val seq: Int? = null,
    // 아이콘 URL
    @Column(name = "url", length = 200)
    val url: String? = null,
    // 날짜 정보
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IconEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "IconEntity(id=$id, name=$name)"
}
