package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 상품 랭킹 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 특정 랭킹 스펙에 따른 상품들의 순위 정보
 * - 랭킹 스펙별로 상품 순위를 기록
 */
@Entity
@Table(name = "shopping_productranking")
@Immutable
data class ProductRankingEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 랭킹 스펙 ID (FK to shopping_rankingspecification)
    @Column(name = "specification_id")
    val specificationId: Long? = null,
    // 상품 ID (FK to shopping_product)
    @Column(name = "product_id")
    val productId: Long? = null,
    // 순위 (낮을수록 상위 랭킹)
    @Column(name = "rank")
    val rank: Int? = null,
    // 총점 (랭킹 계산에 사용된 점수)
    @Column(name = "total_score")
    val totalScore: Double? = null,
    // 날짜 정보
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductRankingEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "ProductRankingEntity(id=$id, specificationId=$specificationId, productId=$productId, rank=$rank)"
}
