package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.ProductCategorySetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 상품-카테고리 관계 JPA Repository
 *
 * zelda-product의 db/queries/product_category_set.sql 기반
 * - GetProductCategorySetByProductID: 상품 ID로 카테고리 관계 조회
 * - GetProductCategorySetByCategoryIDAndAfterID: 카테고리 ID로 상품 ID 목록 조회 (커서 페이징)
 */
@Repository
interface ProductCategorySetJpaRepository : JpaRepository<ProductCategorySetEntity, Long> {
    /**
     * 상품 ID로 카테고리 관계 조회
     *
     * SQL: GetProductCategorySetByProductID
     */
    @Query(
        """
        SELECT pcs FROM ProductCategorySetEntity pcs
        WHERE pcs.productId = :productId
        ORDER BY pcs.id
    """,
    )
    fun findByProductId(
        @Param("productId") productId: Long,
    ): List<ProductCategorySetEntity>

    /**
     * 카테고리 ID로 상품 ID 목록 조회 (커서 페이징)
     *
     * SQL: GetProductCategorySetByCategoryIDAndAfterID
     */
    @Query(
        value = """
        SELECT product_id FROM shopping_product_category_set
        WHERE category_id = :categoryId
        AND product_id > :afterProductId
        ORDER BY product_id
        LIMIT :limit
    """,
        nativeQuery = true,
    )
    fun findProductIdsByCategoryId(
        @Param("categoryId") categoryId: Long,
        @Param("afterProductId") afterProductId: Long = 0,
        @Param("limit") limit: Int,
    ): List<Long>
}
