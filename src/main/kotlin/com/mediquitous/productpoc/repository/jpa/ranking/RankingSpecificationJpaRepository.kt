package com.mediquitous.productpoc.repository.jpa.ranking

import com.mediquitous.productpoc.repository.jpa.ranking.entity.RankingSpecificationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 랭킹 스펙 JPA Repository
 *
 * Go 서비스의 GetRankingSpecificationByPath 쿼리와 동일한 스펙 구현
 */
@Repository
interface RankingSpecificationJpaRepository : JpaRepository<RankingSpecificationEntity, Long> {
    /**
     * 경로로 랭킹 스펙 ID 조회
     *
     * Go SQL 원본:
     * ```sql
     * SELECT id
     * FROM shopping_rankingspecification
     * WHERE path = $1
     * LIMIT 1;
     * ```
     *
     * @param path 랭킹 경로 (예: "popular", "best-seller", "new-arrival")
     * @return 랭킹 스펙 ID (없으면 null)
     */
    @Query(
        """
        SELECT rs.id FROM RankingSpecificationEntity rs
        WHERE rs.path = :path
    """,
    )
    fun findIdByPath(
        @Param("path") path: String,
    ): Long?

    /**
     * 경로로 랭킹 스펙 엔티티 조회
     *
     * @param path 랭킹 경로
     * @return 랭킹 스펙 엔티티 (없으면 null)
     */
    fun findByPath(path: String): RankingSpecificationEntity?
}
