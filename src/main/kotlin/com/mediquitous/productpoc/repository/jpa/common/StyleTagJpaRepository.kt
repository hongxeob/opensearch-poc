package com.mediquitous.productpoc.repository.jpa.common

import com.mediquitous.productpoc.repository.jpa.common.entity.StyleTagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 스타일 태그 JPA Repository
 *
 * zelda-product의 db/queries/styletag.sql 기반
 * - GetStyleTagBySellerID: 셀러 ID로 스타일 태그 조회 (SellerStyleTag JOIN)
 */
@Repository
interface StyleTagJpaRepository : JpaRepository<StyleTagEntity, Long> {
    /**
     * 셀러 ID로 스타일 태그 조회 (SellerStyleTag JOIN)
     *
     * SQL: GetStyleTagBySellerID
     */
    @Query(
        """
        SELECT st FROM StyleTagEntity st
        JOIN SellerStyleTagEntity sst ON st.id = sst.styleTagId
        WHERE sst.sellerId = :sellerId
        ORDER BY st.id
    """,
    )
    fun findBySellerId(
        @Param("sellerId") sellerId: Long,
    ): List<StyleTagEntity>
}
