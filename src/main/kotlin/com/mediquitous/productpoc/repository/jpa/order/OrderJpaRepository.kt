package com.mediquitous.productpoc.repository.jpa.order

import com.mediquitous.productpoc.repository.jpa.order.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 주문 JPA Repository
 *
 * Go 서비스의 주문 관련 쿼리와 동일한 스펙 구현
 * - GetRecentlyViewedSellerIDsByCustomerID
 */
@Repository
interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {
    /**
     * 고객이 최근 구매한 셀러 ID 목록 조회
     *
     * Go SQL 원본:
     * ```sql
     * SELECT p.seller_id
     * FROM shopping_orderitem oi
     * JOIN shopping_order o ON oi.order_id = o.id
     * JOIN shopping_productvariant pv ON oi.product_variant_id = pv.id
     * JOIN shopping_product p ON pv.product_id = p.id
     * JOIN shopping_seller ss ON p.seller_id = ss.id
     * WHERE o.customer_id = @customer_id::bigint
     *   AND p.seller_id IS NOT NULL
     *   AND ss.display = TRUE
     *   AND ss.status = 'normal'
     *   AND (cardinality(@exclude_seller_ids) = 0 OR NOT (p.seller_id = ANY (@exclude_seller_ids)))
     * GROUP BY p.seller_id
     * ORDER BY MAX(oi.id) DESC
     * LIMIT @limit_count;
     * ```
     *
     * 조회 조건:
     * - 특정 고객의 주문 품목 조회
     * - 노출 가능하고 정상 상태인 셀러의 상품만
     * - 제외할 셀러 ID 목록이 있으면 제외
     * - 셀러별로 그룹화하여 최근 주문 순으로 정렬
     *
     * @param customerId 고객 ID
     * @param excludeSellerIds 제외할 셀러 ID 목록 (빈 리스트면 제외 없음)
     * @param limitCount 조회할 최대 개수
     * @return 최근 구매한 셀러 ID 목록 (최근 순)
     */
    @Query(
        value = """
        SELECT p.seller_id
        FROM shopping_orderitem oi
        JOIN shopping_order o ON oi.order_id = o.id
        JOIN shopping_productvariant pv ON oi.product_variant_id = pv.id
        JOIN shopping_product p ON pv.product_id = p.id
        JOIN shopping_seller ss ON p.seller_id = ss.id
        WHERE o.customer_id = :customerId
          AND p.seller_id IS NOT NULL
          AND ss.display = TRUE
          AND ss.status = 'normal'
          AND (COALESCE(array_length(CAST(:excludeSellerIds AS bigint[]), 1), 0) = 0 
               OR p.seller_id <> ALL(CAST(:excludeSellerIds AS bigint[])))
        GROUP BY p.seller_id
        ORDER BY MAX(oi.id) DESC
        LIMIT :limitCount
    """,
        nativeQuery = true,
    )
    fun findRecentlyPurchasedSellerIdsByCustomerId(
        @Param("customerId") customerId: Long,
        @Param("excludeSellerIds") excludeSellerIds: List<Long> = emptyList(),
        @Param("limitCount") limitCount: Int = 10,
    ): List<Long>
}
