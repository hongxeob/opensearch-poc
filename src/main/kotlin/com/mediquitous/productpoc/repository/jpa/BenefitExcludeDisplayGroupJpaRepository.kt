package com.mediquitous.productpoc.repository.jpa

import com.mediquitous.productpoc.repository.jpa.entity.BenefitExcludeDisplayGroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 혜택 제외 기획전 JPA Repository
 *
 * Go 서비스의 GetBenefitExcludeDisplayGroupsByBenefitID 쿼리와 동일한 스펙 구현
 */
@Repository
interface BenefitExcludeDisplayGroupJpaRepository : JpaRepository<BenefitExcludeDisplayGroupEntity, Long> {
    /**
     * 여러 혜택 ID로 제외 기획전 관계 조회
     *
     * Go SQL 원본:
     * ```sql
     * SELECT id, benefit_id, displaygroup_id
     * FROM shopping_benefit_exclude_displaygroups
     * WHERE benefit_id = ANY(sqlc.arg(benefit_ids)::bigint[]);
     * ```
     *
     * @param benefitIds 조회할 혜택 ID 목록
     * @return 해당 혜택들의 제외 기획전 관계 목록
     */
    @Query(
        """
        SELECT bedg FROM BenefitExcludeDisplayGroupEntity bedg
        WHERE bedg.benefitId IN :benefitIds
    """,
    )
    fun findByBenefitIds(
        @Param("benefitIds") benefitIds: List<Long>,
    ): List<BenefitExcludeDisplayGroupEntity>
}
