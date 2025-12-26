package com.mediquitous.productpoc.repository.jpa.customer

import com.mediquitous.productpoc.repository.jpa.customer.entity.LikeEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 좋아요한 상품 Projection
 */
interface LikedProductProjection {
    val likeId: Long
    val productId: Long
}

/**
 * 좋아요 JPA Repository
 *
 * zelda-product의 db/queries/like.sql 기반
 * - GetLikeCountBySellerID: 셀러 ID로 좋아요 개수 조회
 * - GetLikedProductIDsByCustomerID: 고객이 좋아요한 상품 ID 목록 조회
 * - GetLikedProductIDsByCustomerIDWithCursor: 고객이 좋아요한 상품 ID 목록 조회 (커서 페이징)
 * - GetLikedSellerIDsByCustomerID: 고객이 좋아요한 셀러 ID 목록 조회
 */
@Repository
interface LikeJpaRepository : JpaRepository<LikeEntity, Long> {
    /**
     * 셀러 ID로 좋아요 개수 조회
     *
     * SQL: GetLikeCountBySellerID
     */
    @Query(
        """
        SELECT COUNT(l) FROM LikeEntity l
        WHERE l.sellerId = :sellerId
    """,
    )
    fun countBySellerId(
        @Param("sellerId") sellerId: Long,
    ): Long

    /**
     * 고객이 좋아요한 상품 ID 목록 조회 (display 상품만)
     *
     * SQL: GetLikedProductIDsByCustomerID
     */
    @Query(
        """
        SELECT l.id AS likeId, l.productId AS productId
        FROM LikeEntity l
        LEFT JOIN ProductEntity p ON l.productId = p.id
        WHERE l.customerId = :customerId
        AND l.target = 'product'
        AND p.display IS NOT NULL
        AND p.deleted IS NULL
        ORDER BY l.id DESC
    """,
    )
    fun findLikedProductIdsByCustomerId(
        @Param("customerId") customerId: Long,
        pageable: Pageable,
    ): List<LikedProductProjection>

    /**
     * 고객이 좋아요한 상품 ID 목록 조회 (커서 페이징)
     *
     * SQL: GetLikedProductIDsByCustomerIDWithCursor
     */
    @Query(
        """
        SELECT l.id AS likeId, l.productId AS productId
        FROM LikeEntity l
        LEFT JOIN ProductEntity p ON l.productId = p.id
        WHERE l.customerId = :customerId
        AND l.target = 'product'
        AND p.display IS NOT NULL
        AND p.deleted IS NULL
        AND l.id < :cursor
        ORDER BY l.id DESC
    """,
    )
    fun findLikedProductIdsByCustomerIdWithCursor(
        @Param("customerId") customerId: Long,
        @Param("cursor") cursor: Long,
        pageable: Pageable,
    ): List<LikedProductProjection>

    /**
     * 고객이 좋아요한 셀러 ID 목록 조회
     *
     * SQL: GetLikedSellerIDsByCustomerID
     */
    @Query(
        """
        SELECT sl.sellerId
        FROM LikeEntity sl
        JOIN SellerEntity ss ON sl.sellerId = ss.id
        WHERE sl.customerId = :customerId
        AND sl.target = 'seller'
        AND ss.display = true
        AND ss.status = 'normal'
        AND (:excludeSellerIdsEmpty = true OR sl.sellerId NOT IN :excludeSellerIds)
        ORDER BY sl.created DESC
    """,
    )
    fun findLikedSellerIdsByCustomerId(
        @Param("customerId") customerId: Long,
        @Param("excludeSellerIds") excludeSellerIds: List<Long>,
        @Param("excludeSellerIdsEmpty") excludeSellerIdsEmpty: Boolean,
        @Param("limitCount") limitCount: Int,
    ): List<Long>
}
