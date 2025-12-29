package com.mediquitous.productpoc.service.migration

/**
 * 상품 마이그레이션 서비스
 *
 * PostgreSQL → OpenSearch 마이그레이션 및 이벤트 버퍼 관리
 */
interface ProductMigrationService {
    /**
     * 전체 상품 마이그레이션
     *
     * PostgreSQL의 모든 상품을 OpenSearch로 마이그레이션합니다.
     * 배치 단위로 상품 ID를 조회하여 이벤트 버퍼에 추가합니다.
     */
    fun migrateAll()

    /**
     * 특정 상품 ID 목록 마이그레이션
     *
     * @param productIds 마이그레이션할 상품 ID 목록
     */
    fun migrateByIds(productIds: List<Long>)

    /**
     * 이벤트 버퍼 Flush
     *
     * Redis에 쌓인 상품 ID를 꺼내서 Kafka 이벤트를 발행합니다.
     *
     * @param count 처리할 이벤트 개수
     */
    fun flushEventBuffer(count: Int)
}
