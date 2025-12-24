package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 셀러-스타일 태그 매핑 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 셀러와 스타일 태그의 다대다 관계 테이블
 * - 셀러가 취급하는 스타일 카테고리 표현
 * - seq 값으로 표시 순서 제어
 */
@Entity
@Table(name = "shopping_sellerstyletag")
@Immutable
data class SellerStyleTagEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 셀러 ID (FK to shopping_seller)
    @Column(name = "seller_id")
    val sellerId: Long? = null,
    // 스타일 태그 ID (FK to nugustyling_styletag)
    @Column(name = "style_tag_id")
    val styleTagId: Long? = null,
    // 표시 순서 (0 이상)
    @Column(name = "seq")
    val seq: Int? = null,
    // 날짜 정보
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SellerStyleTagEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "SellerStyleTagEntity(id=$id, sellerId=$sellerId, styleTagId=$styleTagId, seq=$seq)"
}
