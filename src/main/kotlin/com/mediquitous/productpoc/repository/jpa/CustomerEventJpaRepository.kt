package com.mediquitous.productpoc.repository.jpa

import com.mediquitous.productpoc.repository.jpa.entity.CustomerEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

/**
 * 고객 이벤트 JPA Repository
 *
 * Go 서비스의 최근 조회 이력 관련 쿼리와 동일한 스펙 구현
 */
@Repository
interface CustomerEventJpaRepository : JpaRepository<CustomerEventEntity, Long> {
    /**
     * 고객이 최근 조회한 셀러 ID 목록 조회
     *
     * Go SQL 원본:
     * ```sql
     * SELECT p.seller_id
     * FROM shopping_customerevent ce
     * JOIN shopping_product p ON ce.product_id = p.id
     * JOIN shopping_seller ss ON p.seller_id = ss.id
     * WHERE ce.customer_id = @customer_id::bigint
     *   AND ce.action = 'view_item'
     *   AND p.seller_id IS NOT NULL
     *   AND ss.display = TRUE
     *   AND ss.status = 'normal'
     *   AND (cardinality(@exclude_seller_ids) = 0 OR NOT (p.seller_id = ANY (@exclude_seller_ids)))
     * GROUP BY p.seller_id
     * ORDER BY MAX(ce.created) DESC
     * LIMIT @limit_count;
     * ```
     *
     * @param customerId 고객 ID
     * @param excludeSellerIds 제외할 셀러 ID 목록
     * @param limitCount 조회할 최대 개수
     * @return 최근 조회한 셀러 ID 목록 (최근 순)
     */
    @Query(
        value = """
        SELECT p.seller_id
        FROM shopping_customerevent ce
        JOIN shopping_product p ON ce.product_id = p.id
        JOIN shopping_seller ss ON p.seller_id = ss.id
        WHERE ce.customer_id = :customerId
          AND ce.action = 'view_item'
          AND p.seller_id IS NOT NULL
          AND ss.display = TRUE
          AND ss.status = 'normal'
          AND (COALESCE(array_length(CAST(:excludeSellerIds AS bigint[]), 1), 0) = 0 
               OR p.seller_id <> ALL(CAST(:excludeSellerIds AS bigint[])))
        GROUP BY p.seller_id
        ORDER BY MAX(ce.created) DESC
        LIMIT :limitCount
    """,
        nativeQuery = true,
    )
    fun findRecentlyViewedSellerIdsByCustomerId(
        @Param("customerId") customerId: Long,
        @Param("excludeSellerIds") excludeSellerIds: List<Long> = emptyList(),
        @Param("limitCount") limitCount: Int = 10,
    ): List<Long>

    /**
     * 고객이 최근 조회한 상품 ID 목록 조회 (표시 가능한 상품만)
     *
     * Go SQL 원본:
     * ```sql
     * SELECT p.id, sub.last_viewed
     * FROM shopping_product p
     * JOIN (
     *     SELECT ce.product_id, MAX(ce.created) as last_viewed
     *     FROM shopping_customerevent ce
     *     WHERE ce.customer_id = @customer_id::bigint
     *       AND ce.action = 'view_item'
     *     GROUP BY ce.product_id
     *     ORDER BY last_viewed DESC
     *     LIMIT @limit_count
     * ) sub ON p.id = sub.product_id
     * WHERE p.selling IS NOT NULL
     *   AND p.display IS NOT NULL
     *   AND p.image_id IS NOT NULL
     *   AND p.deleted IS NULL
     * ORDER BY sub.last_viewed DESC;
     * ```
     *
     * 조회 조건:
     * - view_item 액션 이벤트만 조회
     * - 판매 중이고, 노출되고, 이미지가 있고, 삭제되지 않은 상품만
     * - 최근 조회 순으로 정렬
     *
     * @param customerId 고객 ID
     * @param limitCount 조회할 최대 개수
     * @return 최근 조회한 상품 ID와 조회 시각 목록
     */
    @Query(
        value = """
        SELECT p.id, sub.last_viewed
        FROM shopping_product p
        JOIN (
            SELECT ce.product_id, MAX(ce.created) as last_viewed
            FROM shopping_customerevent ce
            WHERE ce.customer_id = :customerId
              AND ce.action = 'view_item'
            GROUP BY ce.product_id
            ORDER BY last_viewed DESC
            LIMIT :limitCount
        ) sub ON p.id = sub.product_id
        WHERE p.selling IS NOT NULL
          AND p.display IS NOT NULL
          AND p.image_id IS NOT NULL
          AND p.deleted IS NULL
        ORDER BY sub.last_viewed DESC
    """,
        nativeQuery = true,
    )
    fun findRecentlyViewedProductIdsByCustomerId(
        @Param("customerId") customerId: Long,
        @Param("limitCount") limitCount: Int = 20,
    ): List<Array<Any>>
}
