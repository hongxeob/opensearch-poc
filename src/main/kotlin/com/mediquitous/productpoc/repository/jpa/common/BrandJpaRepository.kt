package com.mediquitous.productpoc.repository.jpa.common

import com.mediquitous.productpoc.repository.jpa.common.entity.BrandEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 브랜드 JPA Repository
 */
@Repository
interface BrandJpaRepository : JpaRepository<BrandEntity, Long> {
    /**
     * ID로 브랜드 조회
     */
    fun findNullableById(id: Long): BrandEntity?

    /**
     * 여러 ID로 브랜드 조회
     */
    @Query(
        """
        SELECT b FROM BrandEntity b
        WHERE b.id IN :ids
        AND b.deleted IS NULL
        ORDER BY b.id
    """,
    )
    fun findByIds(
        @Param("ids") ids: List<Long>,
    ): List<BrandEntity>

    /**
     * 브랜드 코드로 조회
     */
    fun findByCode(code: String): BrandEntity?
}
