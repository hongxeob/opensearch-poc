package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 상품 JPA Repository
 *
 * zelda-product의 db/queries/product.sql 기반
 */
@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, Long> {
    /**
     * ID로 상품 조회
     *
     * SQL: GetProductByID
     */
    fun findNullableById(id: Long): ProductEntity?

    /**
     * 여러 ID로 상품 조회 (삭제되지 않은 상품만)
     */
    @Query(
        """
        SELECT p FROM ProductEntity p
        WHERE p.id IN :ids
        AND p.deleted IS NULL
        ORDER BY p.id
    """,
    )
    fun findByIdsAndNotDeleted(
        @Param("ids") ids: List<Long>,
    ): List<ProductEntity>

    /**
     * 가이드 이미지 ID로 상품 조회
     *
     * SQL: GetProductByGuideImageID
     */
    fun findByGuideImageId(guideImageId: Long): ProductEntity?

    /**
     * 셀러 ID로 상품 ID 목록 조회 (커서 페이징)
     *
     * SQL: GetProductIDsBySellerID
     *
     * @param sellerId 셀러 ID
     * @param afterId 커서 (이 ID 이후부터 조회)
     * @param limit 조회 개수
     */
    @Query(
        """
        SELECT p.id FROM ProductEntity p
        WHERE p.sellerId = :sellerId
        AND p.id > :afterId
        ORDER BY p.id
    """,
    )
    fun findProductIdsBySellerId(
        @Param("sellerId") sellerId: Long,
        @Param("afterId") afterId: Long = 0,
        @Param("limit") limit: Int,
    ): List<Long>

    /**
     * ID 이후의 상품 ID 목록 조회 (커서 페이징)
     *
     * SQL: GetProductIDsByAfterID
     *
     * @param afterId 커서 (이 ID 이후부터 조회)
     * @param limit 조회 개수
     */
    @Query(
        """
        SELECT p.id FROM ProductEntity p
        WHERE p.id > :afterId
        ORDER BY p.id
    """,
    )
    fun findProductIdsAfter(
        @Param("afterId") afterId: Long,
        @Param("limit") limit: Int,
    ): List<Long>
}
