package com.mediquitous.productpoc.repository.jpa

import com.mediquitous.productpoc.repository.jpa.entity.SellerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 셀러 JPA Repository
 *
 * zelda-product의 db/queries/seller.sql 기반
 * - GetSellerByID: ID로 셀러 조회
 * - GetSellerIDsByAfterID: ID 이후의 셀러 ID 목록 조회 (커서 페이징)
 */
@Repository
interface SellerJpaRepository : JpaRepository<SellerEntity, Long> {
    /**
     * ID로 셀러 조회
     *
     * SQL: GetSellerByID
     * Spring Data JPA의 findById()를 사용하거나 nullable 조회를 위해 커스텀 메서드 사용
     */
    fun findNullableById(id: Long): SellerEntity?

    /**
     * ID 이후의 셀러 ID 목록 조회 (커서 페이징)
     *
     * SQL: GetSellerIDsByAfterID
     *
     * @param afterId 커서 (이 ID 이후부터 조회)
     * @param limit 조회 개수
     */
    @Query(
        """
        SELECT s.id FROM SellerEntity s
        WHERE s.id > :afterId
        ORDER BY s.id
    """,
    )
    fun findSellerIdsAfter(
        @Param("afterId") afterId: Long,
        @Param("limit") limit: Int,
    ): List<Long>
}
