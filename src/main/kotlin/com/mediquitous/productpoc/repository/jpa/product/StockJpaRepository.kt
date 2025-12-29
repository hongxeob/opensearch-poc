package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.StockEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 재고 + Warehouse + RetailStore JOIN 결과
 * Go의 GetStockByVariantIDsRow에 해당
 */
interface StockWithWarehouse {
    val id: Long
    val quantity: Int
    val productVariantId: Long
    val warehouseId: Long
    val warehouseName: String
    val retailStoreName: String?
    val quick: Boolean
}

/**
 * 재고 JPA Repository
 *
 * zelda-product의 db/queries/stock.sql 기반
 * - GetStockByVariantIDs: 여러 품목 ID로 재고 조회 (Warehouse, RetailStore JOIN)
 *
 * Note: Go SQL에서는 Warehouse와 RetailStore를 JOIN하지만,
 * Spring에서는 Entity의 연관관계 매핑을 통해 처리하거나 필요시 별도 조회합니다.
 */
@Repository
interface StockJpaRepository : JpaRepository<StockEntity, Long> {
    /**
     * 여러 품목 ID로 재고 조회
     *
     * SQL: GetStockByVariantIDs
     */
    @Query(
        """
        SELECT 
            s.id as id,
            s.quantity as quantity,
            s.productVariantId as productVariantId,
            s.warehouseId as warehouseId,
            w.name as warehouseName,
            r.name as retailStoreName,
            w.quick as quick
        FROM StockEntity s
        JOIN WarehouseEntity w ON w.id = s.warehouseId
        LEFT JOIN RetailStoreEntity r ON r.warehouseId = w.id
        WHERE s.productVariantId IN :variantIds
        ORDER BY s.productVariantId, s.id
    """,
    )
    fun findByProductVariantIds(
        @Param("variantIds") variantIds: List<Long>,
    ): List<StockWithWarehouse>
}
