package com.mediquitous.productpoc.repository.jpa.benefit

import com.mediquitous.productpoc.repository.jpa.benefit.entity.BenefitProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 혜택-상품 매핑 JPA Repository
 *
 * Go 서비스의 GetActiveBenefitProductByProductID 쿼리와 동일한 스펙 구현
 */
@Repository
interface BenefitProductJpaRepository : JpaRepository<BenefitProductEntity, Long> {
    /**
     * 특정 혜택의 활성화된 상품 목록 조회
     *
     * Go SQL 원본:
     * ```sql
     * SELECT id, deleted, discount_amount, created, updated, benefit_id, product_id
     * FROM shopping_benefitproduct
     * WHERE benefit_id = $1 AND deleted IS NULL;
     * ```
     *
     * @param benefitId 조회할 혜택 ID
     * @return 해당 혜택에 속한 활성화된 상품 목록
     */
    @Query(
        """
        SELECT bp FROM BenefitProductEntity bp
        WHERE bp.benefitId = :benefitId
        AND bp.deleted IS NULL
    """,
    )
    fun findActiveBenefitProductsByBenefitId(
        @Param("benefitId") benefitId: Long,
    ): List<BenefitProductEntity>
}
