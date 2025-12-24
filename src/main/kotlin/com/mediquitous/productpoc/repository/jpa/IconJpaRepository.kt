package com.mediquitous.productpoc.repository.jpa

import com.mediquitous.productpoc.repository.jpa.entity.IconEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 아이콘 JPA Repository
 *
 * zelda-product의 db/queries/icon.sql 기반
 * - GetIconByProductID: 상품 ID로 아이콘 조회 (ProductIconSet JOIN)
 */
@Repository
interface IconJpaRepository : JpaRepository<IconEntity, Long> {
    /**
     * 상품 ID로 아이콘 목록 조회 (ProductIconSet JOIN, seq 순서대로)
     *
     * SQL: GetIconByProductID
     */
    @Query(
        """
        SELECT i FROM IconEntity i
        JOIN ProductIconSetEntity pis ON i.id = pis.iconId
        WHERE pis.productId = :productId
        ORDER BY i.seq
    """,
    )
    fun findByProductId(
        @Param("productId") productId: Long,
    ): List<IconEntity>
}
