package com.mediquitous.productpoc.repository.jpa.ranking

import com.mediquitous.productpoc.repository.jpa.ranking.entity.ProductRankingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 랭킹 상품 Projection
 */
interface RankedProductProjection {
    val productId: Long
    val rank: Int
}

/**
 * 상품 랭킹 JPA Repository
 *
 * Go 서비스의 상품 랭킹 조회 쿼리와 동일한 스펙 구현
 * - GetRankedProductIDsByCursor
 * - GetRankedProductIDs
 */
@Repository
interface ProductRankingJpaRepository : JpaRepository<ProductRankingEntity, Long> {
    /**
     * 커서 기반 페이지네이션으로 랭킹 상품 ID 조회
     *
     * Go SQL 원본:
     * ```sql
     * SELECT product_id, rank
     * FROM shopping_productranking
     * WHERE specification_id = $1
     *   AND rank > $2
     * ORDER BY rank ASC
     * LIMIT $3;
     * ```
     *
     * @param specificationId 랭킹 스펙 ID
     * @param cursor 커서 (마지막 조회한 rank 값)
     * @param limit 조회할 개수
     * @return 상품 ID와 순위 목록
     */
    @Query(
        value = """
        SELECT product_id AS productId, rank AS rank
        FROM shopping_productranking
        WHERE specification_id = :specificationId
          AND rank > :cursor
        ORDER BY rank ASC
        LIMIT :limit
    """,
        nativeQuery = true,
    )
    fun findRankedProductIdsByCursor(
        @Param("specificationId") specificationId: Long,
        @Param("cursor") cursor: Int,
        @Param("limit") limit: Int,
    ): List<RankedProductProjection>

    /**
     * 전체 랭킹 상품 ID 조회 (첫 페이지용)
     *
     * Go SQL 원본:
     * ```sql
     * SELECT product_id, rank
     * FROM shopping_productranking
     * WHERE specification_id = $1
     * ORDER BY rank ASC
     * LIMIT $2;
     * ```
     *
     * @param specificationId 랭킹 스펙 ID
     * @param limit 조회할 개수
     * @return 상품 ID와 순위 목록
     */
    @Query(
        value = """
        SELECT product_id AS productId, rank AS rank
        FROM shopping_productranking
        WHERE specification_id = :specificationId
        ORDER BY rank ASC
        LIMIT :limit
    """,
        nativeQuery = true,
    )
    fun findRankedProductIds(
        @Param("specificationId") specificationId: Long,
        @Param("limit") limit: Int,
    ): List<RankedProductProjection>
}
