package com.mediquitous.productpoc.repository.jpa

import com.mediquitous.productpoc.repository.jpa.entity.DisplayGroupProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 기획전-상품 관계 JPA Repository
 *
 * zelda-product의 db/queries/displaygroupproduct.sql 기반
 * - GetProductIDsByDisplayGroupID: 기획전 ID로 상품 ID 목록 조회 (커서 페이징)
 * - GetProductIDsByBenefitID: 혜택 ID로 상품 ID 목록 조회 (Benefit JOIN, 커서 페이징)
 * - GetProductIDsByCouponSettingID: 쿠폰 설정 ID로 상품 ID 목록 조회 (CouponSetting JOIN, 커서 페이징)
 * - GetDisplayGroupProductByProductID: 상품 ID로 기획전 관계 조회
 */
@Repository
interface DisplayGroupProductJpaRepository : JpaRepository<DisplayGroupProductEntity, Long> {
    /**
     * 기획전 ID로 상품 ID 목록 조회 (커서 페이징)
     *
     * SQL: GetProductIDsByDisplayGroupID
     */
    @Query(
        """
        SELECT dgp.productId FROM DisplayGroupProductEntity dgp
        WHERE dgp.groupId = :groupId
        AND dgp.productId > :afterProductId
        ORDER BY dgp.productId
    """,
    )
    fun findProductIdsByGroupId(
        @Param("groupId") groupId: Long,
        @Param("afterProductId") afterProductId: Long = 0,
        @Param("limit") limit: Int,
    ): List<Long>

    /**
     * 혜택 ID로 상품 ID 목록 조회 (Benefit JOIN, 커서 페이징)
     *
     * SQL: GetProductIDsByBenefitID
     */
    @Query(
        """
        SELECT dgp.productId FROM DisplayGroupProductEntity dgp
        JOIN BenefitEntity b ON b.displayGroupId = dgp.groupId
        WHERE b.id = :benefitId
        AND dgp.productId > :afterProductId
        ORDER BY dgp.productId
    """,
    )
    fun findProductIdsByBenefitId(
        @Param("benefitId") benefitId: Long,
        @Param("afterProductId") afterProductId: Long = 0,
        @Param("limit") limit: Int,
    ): List<Long>

    /**
     * 쿠폰 설정 ID로 상품 ID 목록 조회 (CouponSetting JOIN, 커서 페이징)
     *
     * SQL: GetProductIDsByCouponSettingID
     */
    @Query(
        """
        SELECT dgp.productId FROM DisplayGroupProductEntity dgp
        JOIN CouponSettingEntity cs ON cs.displayGroupId = dgp.groupId
        WHERE cs.id = :couponSettingId
        AND dgp.productId > :afterProductId
        ORDER BY dgp.productId
    """,
    )
    fun findProductIdsByCouponSettingId(
        @Param("couponSettingId") couponSettingId: Long,
        @Param("afterProductId") afterProductId: Long = 0,
        @Param("limit") limit: Int,
    ): List<Long>

    /**
     * 상품 ID로 기획전 관계 조회
     *
     * SQL: GetDisplayGroupProductByProductID
     */
    @Query(
        """
        SELECT dgp FROM DisplayGroupProductEntity dgp
        WHERE dgp.productId = :productId
        ORDER BY dgp.id
    """,
    )
    fun findByProductId(
        @Param("productId") productId: Long,
    ): List<DisplayGroupProductEntity>
}
