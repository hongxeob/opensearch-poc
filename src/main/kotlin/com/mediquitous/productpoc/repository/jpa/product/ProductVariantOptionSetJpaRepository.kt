package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.ProductVariantOptionSetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 품목-옵션 관계 JPA Repository
 * zelda-product의 db/queries/seller.sql 기반
 * Go 서비스의 GetProductVariantOptionSetByProductVariantID 쿼리와 동일한 스펙 구현
 */
@Repository
interface ProductVariantOptionSetJpaRepository : JpaRepository<ProductVariantOptionSetEntity, Long> {
    /**
     * 여러 품목 ID로 옵션 관계 조회
     *
     * Go SQL 원본:
     * ```sql
     * SELECT id, productvariant_id, option_id
     * FROM shopping_productvariant_option_set
     * WHERE productvariant_id = ANY(sqlc.arg(product_variant_ids)::bigint[]);
     * ```
     *
     * @param variantIds 조회할 품목 ID 목록
     * @return 해당 품목들의 옵션 관계 목록
     */
    @Query(
        """
        SELECT pvos FROM ProductVariantOptionSetEntity pvos
        WHERE pvos.productVariantId IN :variantIds
    """,
    )
    fun findByProductVariantIds(
        @Param("variantIds") variantIds: List<Long>,
    ): List<ProductVariantOptionSetEntity>
}
