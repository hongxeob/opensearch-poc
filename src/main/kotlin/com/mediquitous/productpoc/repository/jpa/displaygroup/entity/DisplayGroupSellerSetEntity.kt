package com.mediquitous.productpoc.repository.jpa.displaygroup.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 기획전-셀러 매핑 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 기획전(DisplayGroup)과 셀러(Seller)의 다대다 관계 테이블
 * - 특정 기획전에 참여하는 셀러들을 관리
 * - 셀러별 기획전 필터링 및 참여 기획전 조회에 활용
 */
@Entity
@Table(name = "shopping_displaygroup_seller_set")
@Immutable
data class DisplayGroupSellerSetEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 기획전 ID (FK to shopping_displaygroup)
    @Column(name = "displaygroup_id")
    val displayGroupId: Long? = null,
    // 셀러 ID (FK to shopping_seller)
    @Column(name = "seller_id")
    val sellerId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DisplayGroupSellerSetEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "DisplayGroupSellerSetEntity(id=$id, displayGroupId=$displayGroupId, sellerId=$sellerId)"
}
