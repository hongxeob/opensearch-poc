package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.ProductRelatedProductsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 연관 상품 JPA Repository
 *
 * zelda-product의 db/queries/product_related_products.sql 기반
 * - GetRelatedProductByFromProductID: 기준 상품 ID로 연관 상품 조회
 */
@Repository
interface ProductRelatedProductsJpaRepository : JpaRepository<ProductRelatedProductsEntity, Long> {
    /**
     * 기준 상품 ID로 연관 상품 조회
     *
     * SQL: GetRelatedProductByFromProductID
     */
    @Query(
        """
        SELECT prp FROM ProductRelatedProductsEntity prp
        WHERE prp.fromProductId = :fromProductId
        ORDER BY prp.id
    """,
    )
    fun findByFromProductId(
        @Param("fromProductId") fromProductId: Long,
    ): List<ProductRelatedProductsEntity>
}
