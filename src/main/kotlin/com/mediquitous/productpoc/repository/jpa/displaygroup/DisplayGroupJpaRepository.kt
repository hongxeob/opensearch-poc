package com.mediquitous.productpoc.repository.jpa.displaygroup

import com.mediquitous.productpoc.repository.jpa.displaygroup.entity.DisplayGroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 기획전 JPA Repository
 *
 * zelda-product의 db/queries/displaygroup.sql 기반
 * - GetActiveDisplayGroupByProductID: 상품 ID로 활성 기획전 조회 (manual 타입)
 * - GetActiveDisplayGroupByCategoryIDs: 카테고리 ID로 활성 기획전 조회 (auto 타입)
 * - GetActiveDisplayGroupBySellerID: 셀러 ID로 활성 기획전 조회 (auto 타입)
 * - GetActiveDisplayGroupByParentID: 부모 ID로 활성 기획전 조회 (auto 타입)
 * - GetActiveDisplayGroupIDsByIDs: 여러 ID로 활성 기획전 ID 조회
 */
@Repository
interface DisplayGroupJpaRepository : JpaRepository<DisplayGroupEntity, Long> {
    /**
     * 상품 ID로 활성 기획전 조회 (manual 타입, DisplayGroupProduct JOIN)
     *
     * SQL: GetActiveDisplayGroupByProductID
     *
     * Note: product_seq(dgp.seq)는 DisplayGroupProduct 엔티티에서 조회 가능
     */
    @Query(
        """
        SELECT dg FROM DisplayGroupEntity dg
        JOIN DisplayGroupProductEntity dgp ON dg.id = dgp.groupId
        WHERE dgp.productId = :productId
        AND dg.deleted IS NULL
        AND dg.type = 'manual'
        ORDER BY dg.seq
    """,
    )
    fun findActiveDisplayGroupsByProductId(
        @Param("productId") productId: Long,
    ): List<DisplayGroupEntity>

    /**
     * 여러 카테고리 ID로 활성 기획전 조회 (auto 타입)
     *
     * SQL: GetActiveDisplayGroupByCategoryIDs
     */
    @Query(
        """
        SELECT DISTINCT dg FROM DisplayGroupEntity dg
        JOIN DisplayGroupCategorySetEntity dgcs ON dg.id = dgcs.displayGroupId
        WHERE dgcs.categoryId IN :categoryIds
        AND dg.deleted IS NULL
        AND dg.type = 'auto'
        ORDER BY dg.seq
    """,
    )
    fun findActiveDisplayGroupsByCategoryIds(
        @Param("categoryIds") categoryIds: List<Long>,
    ): List<DisplayGroupEntity>

    /**
     * 셀러 ID로 활성 기획전 조회 (auto 타입)
     *
     * SQL: GetActiveDisplayGroupBySellerID
     */
    @Query(
        """
        SELECT dg FROM DisplayGroupEntity dg
        JOIN DisplayGroupSellerSetEntity dgss ON dg.id = dgss.displayGroupId
        WHERE dgss.sellerId = :sellerId
        AND dg.deleted IS NULL
        AND dg.type = 'auto'
        ORDER BY dg.seq
    """,
    )
    fun findActiveDisplayGroupsBySellerId(
        @Param("sellerId") sellerId: Long,
    ): List<DisplayGroupEntity>

    /**
     * 여러 부모 ID로 활성 기획전 조회 (auto 타입)
     *
     * SQL: GetActiveDisplayGroupByParentID
     */
    @Query(
        """
        SELECT dg FROM DisplayGroupEntity dg
        WHERE dg.parentId IN :parentIds
        AND dg.deleted IS NULL
        AND dg.type = 'auto'
        ORDER BY dg.seq
    """,
    )
    fun findActiveDisplayGroupsByParentIds(
        @Param("parentIds") parentIds: List<Long>,
    ): List<DisplayGroupEntity>

    /**
     * 여러 ID로 활성 기획전 ID 목록 조회
     *
     * SQL: GetActiveDisplayGroupIDsByIDs
     */
    @Query(
        """
        SELECT dg.id FROM DisplayGroupEntity dg
        WHERE dg.id IN :ids
        AND dg.deleted IS NULL
    """,
    )
    fun findActiveDisplayGroupIdsByIds(
        @Param("ids") ids: List<Long>,
    ): List<Long>
}
