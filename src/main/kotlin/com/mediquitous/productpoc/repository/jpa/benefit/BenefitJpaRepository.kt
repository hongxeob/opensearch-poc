package com.mediquitous.productpoc.repository.jpa.benefit

import com.mediquitous.productpoc.repository.jpa.benefit.entity.BenefitEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

/**
 * 혜택 JPA Repository
 *
 * zelda-product의 db/queries/benefit.sql 기반
 * - GetActiveBenefitByDisplayGroupIDs: 기획전 ID로 활성 혜택 조회
 * - GetActiveBenefitByProductIDs: 상품 ID로 활성 혜택 조회 (BenefitProduct JOIN)
 */
@Repository
interface BenefitJpaRepository : JpaRepository<BenefitEntity, Long> {
    /**
     * 여러 기획전 ID로 활성 혜택 조회
     *
     * SQL: GetActiveBenefitByDisplayGroupIDs
     *
     * @param displayGroupIds 기획전 ID 목록
     * @param now 현재 시간
     */
    @Query(
        """
        SELECT b FROM BenefitEntity b
        WHERE b.displayGroupId IN :displayGroupIds
        AND b.deleted IS NULL
        AND b.activated = true
        AND b.begin <= :now
        AND b.end > :now
        ORDER BY b.seq
    """,
    )
    fun findActiveBenefitsByDisplayGroupIds(
        @Param("displayGroupIds") displayGroupIds: List<Long>,
        @Param("now") now: OffsetDateTime = OffsetDateTime.now(),
    ): List<BenefitEntity>

    /**
     * 여러 상품 ID로 활성 혜택 조회 (BenefitProduct JOIN)
     *
     * SQL: GetActiveBenefitByProductIDs
     *
     * Note: Go SQL에서는 BenefitProduct의 discount_amount를 함께 조회하지만,
     * Spring에서는 Entity 연관관계를 통해 처리하거나 별도 DTO 프로젝션을 사용합니다.
     *
     * @param productIds 상품 ID 목록
     * @param now 현재 시간
     */
    @Query(
        """
        SELECT b FROM BenefitEntity b
        JOIN ProductBenefitSetEntity bp ON b.id = bp.benefitId
        WHERE bp.productId IN :productIds
        AND b.deleted IS NULL
        AND bp.deleted IS NULL
        AND b.activated = true
        AND b.begin <= :now
        AND b.end > :now
        ORDER BY b.seq
    """,
    )
    fun findActiveBenefitsByProductIds(
        @Param("productIds") productIds: List<Long>,
        @Param("now") now: OffsetDateTime = OffsetDateTime.now(),
    ): List<BenefitEntity>
}
