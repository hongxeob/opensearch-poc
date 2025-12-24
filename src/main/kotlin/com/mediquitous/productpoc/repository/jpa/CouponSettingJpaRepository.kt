package com.mediquitous.productpoc.repository.jpa

import com.mediquitous.productpoc.repository.jpa.entity.CouponSettingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

/**
 * 쿠폰 설정 JPA Repository
 *
 * Go 서비스의 GetActiveCouponSettingByDisplayGroupID 쿼리와 동일한 스펙 구현
 */
@Repository
interface CouponSettingJpaRepository : JpaRepository<CouponSettingEntity, Long> {
    /**
     * 기획전별 활성화된 온라인 다운로드 쿠폰 조회
     *
     * Go SQL 원본:
     * ```sql
     * SELECT * FROM shopping_couponsetting
     * WHERE (displaygroup_id = ANY (sqlc.arg(display_group_ids)::BIGINT[]) OR displaygroup_id IS NULL)
     *   AND deleted IS NULL
     *   AND is_paused = FALSE
     *   AND show_product_detail = TRUE
     *   AND issue_begin <= NOW()
     *   AND issue_end > NOW()
     *   AND type = 'online'
     *   AND benefit_type = 'rate'
     *   AND issue_type = 'download'
     *   AND (issue_max_count IS NULL OR issued_count < issue_max_count)
     *   AND target_type = 'all';
     * ```
     *
     * 조회 조건:
     * - 특정 기획전 또는 전체 상품 대상 쿠폰
     * - 삭제되지 않고 일시정지되지 않은 쿠폰
     * - 상품 상세 페이지에 노출 설정된 쿠폰
     * - 현재 발급 기간 내의 쿠폰
     * - 온라인 쿠폰, 비율 할인, 다운로드 방식
     * - 발급 수량 제한이 없거나 아직 여유가 있는 쿠폰
     * - 전체 회원 대상 쿠폰
     *
     * @param displayGroupIds 조회할 기획전 ID 목록
     * @param now 현재 시각
     * @return 조건을 만족하는 쿠폰 설정 목록
     */
    @Query(
        """
        SELECT cs FROM CouponSettingEntity cs
        WHERE (cs.displayGroupId IN :displayGroupIds OR cs.displayGroupId IS NULL)
        AND cs.deleted IS NULL
        AND cs.isPaused = false
        AND cs.showProductDetail = true
        AND cs.issueBegin <= :now
        AND cs.issueEnd > :now
        AND cs.type = 'online'
        AND cs.benefitType = 'rate'
        AND cs.issueType = 'download'
        AND (cs.issueMaxCount IS NULL OR cs.issuedCount < cs.issueMaxCount)
        AND cs.targetType = 'all'
    """,
    )
    fun findActiveCouponSettingsByDisplayGroupIds(
        @Param("displayGroupIds") displayGroupIds: List<Long>,
        @Param("now") now: OffsetDateTime = OffsetDateTime.now(),
    ): List<CouponSettingEntity>
}
