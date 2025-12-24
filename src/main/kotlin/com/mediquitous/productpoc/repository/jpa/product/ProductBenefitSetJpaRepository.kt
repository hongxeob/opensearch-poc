package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.ProductBenefitSetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 상품 혜택 세트 JPA Repository
 */
@Repository
interface ProductBenefitSetJpaRepository : JpaRepository<ProductBenefitSetEntity, Long> {
    /**
     * 상품 ID로 혜택 관계 조회
     */
    @Query(
        """
        SELECT pbs FROM ProductBenefitSetEntity pbs
        WHERE pbs.productId = :productId
        AND pbs.deleted IS NULL
    """,
    )
    fun findByProductId(
        @Param("productId") productId: Long,
    ): List<ProductBenefitSetEntity>

    /**
     * 여러 상품 ID로 혜택 관계 조회
     */
    @Query(
        """
        SELECT pbs FROM ProductBenefitSetEntity pbs
        WHERE pbs.productId IN :productIds
        AND pbs.deleted IS NULL
        ORDER BY pbs.productId
    """,
    )
    fun findByProductIds(
        @Param("productIds") productIds: List<Long>,
    ): List<ProductBenefitSetEntity>

    /**
     * 혜택 ID로 상품 관계 조회
     */
    @Query(
        """
        SELECT pbs FROM ProductBenefitSetEntity pbs
        WHERE pbs.benefitId = :benefitId
        AND pbs.deleted IS NULL
    """,
    )
    fun findByBenefitId(
        @Param("benefitId") benefitId: Long,
    ): List<ProductBenefitSetEntity>

    /**
     * 혜택 ID로 상품 ID 목록 조회
     */
    @Query(
        """
        SELECT pbs.productId FROM ProductBenefitSetEntity pbs
        WHERE pbs.benefitId = :benefitId
        AND pbs.deleted IS NULL
    """,
    )
    fun findProductIdsByBenefitId(
        @Param("benefitId") benefitId: Long,
    ): List<Long>
}
