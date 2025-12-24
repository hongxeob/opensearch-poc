package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.ProductVariantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 상품 품목 JPA Repository
 *
 * zelda-product의 db/queries/productvariant.sql 기반
 * - GetProductVariantByID: ID로 품목 조회
 * - GetProductVariantByProductID: 상품 ID로 품목 목록 조회
 */
@Repository
interface ProductVariantJpaRepository : JpaRepository<ProductVariantEntity, Long> {
    /**
     * ID로 품목 조회
     *
     * SQL: GetProductVariantByID
     */
    fun findNullableById(id: Long): ProductVariantEntity?

    /**
     * 상품 ID로 품목 목록 조회
     *
     * SQL: GetProductVariantByProductID
     */
    @Query(
        """
        SELECT pv FROM ProductVariantEntity pv
        WHERE pv.productId = :productId
        ORDER BY pv.id
    """,
    )
    fun findByProductId(
        @Param("productId") productId: Long,
    ): List<ProductVariantEntity>
}
