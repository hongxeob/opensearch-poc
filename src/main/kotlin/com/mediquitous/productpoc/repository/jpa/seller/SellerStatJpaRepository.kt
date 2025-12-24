package com.mediquitous.productpoc.repository.jpa.seller

import com.mediquitous.productpoc.repository.jpa.seller.entity.SellerStatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * 셀러 통계 JPA Repository
 *
 * zelda-product의 db/queries/sellerstat.sql 기반
 * - GetSellerStatsBySellerIDAndDateRange: 셀러 ID와 날짜 범위로 일별 통계 조회
 */
@Repository
interface SellerStatJpaRepository : JpaRepository<SellerStatEntity, Long> {
    /**
     * 셀러 ID와 날짜 범위로 일별 통계 조회 (최근 7일)
     *
     * SQL: GetSellerStatsBySellerIDAndDateRange
     *
     * @param sellerId 셀러 ID
     * @param period 기간 타입 ('daily')
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     */
    @Query(
        """
        SELECT ss FROM SellerStatEntity ss
        WHERE ss.sellerId = :sellerId
        AND ss.period = :period
        AND ss.date >= :startDate
        AND ss.date <= :endDate
        ORDER BY ss.date DESC, ss.name ASC
    """,
    )
    fun findBySellerIdAndDateRange(
        @Param("sellerId") sellerId: Long,
        @Param("period") period: String = "daily",
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<SellerStatEntity>
}
