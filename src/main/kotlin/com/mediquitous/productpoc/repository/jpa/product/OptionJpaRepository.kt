package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.OptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 옵션 JPA Repository
 *
 * zelda-product의 db/queries/productoption.sql 기반
 */
@Repository
interface OptionJpaRepository : JpaRepository<OptionEntity, Long> {
    /**
     * 상품 ID로 옵션 목록 조회 (정렬순서 적용)
     *
     * SQL: GetOptionByProductID
     */
    @Query(
        """
        SELECT o FROM OptionEntity o
        WHERE o.productId = :productId
        ORDER BY o.nameSeq, o.valueSeq
    """,
    )
    fun findByProductId(
        @Param("productId") productId: Long,
    ): List<OptionEntity>

    /**
     * 여러 상품 ID로 옵션 목록 조회
     */
    @Query(
        """
        SELECT o FROM OptionEntity o
        WHERE o.productId IN :productIds
        ORDER BY o.productId, o.nameSeq, o.valueSeq
    """,
    )
    fun findByProductIds(
        @Param("productIds") productIds: List<Long>,
    ): List<OptionEntity>
}
