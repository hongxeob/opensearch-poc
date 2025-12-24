package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 랭킹 스펙 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 상품 랭킹 정렬 기준을 정의 (예: 인기순, 판매순, 신상품순 등)
 * - path를 통해 랭킹 타입 식별
 */
@Entity
@Table(name = "shopping_rankingspecification")
@Immutable
data class RankingSpecificationEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 랭킹 경로 (unique, 예: "popular", "best-seller", "new-arrival")
    @Column(name = "path", length = 1000)
    val path: String? = null,
    // 랭킹 설명
    @Column(name = "description", length = 1000)
    val description: String? = null,
    // 날짜 정보
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RankingSpecificationEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "RankingSpecificationEntity(id=$id, path=$path, description=$description)"
}
