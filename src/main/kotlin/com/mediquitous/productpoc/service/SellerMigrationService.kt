package com.mediquitous.productpoc.service

/**
 * 셀러 마이그레이션 서비스
 *
 * PostgreSQL → OpenSearch 셀러 마이그레이션
 */
interface SellerMigrationService {
    /**
     * 전체 셀러 마이그레이션
     *
     * PostgreSQL의 모든 셀러를 OpenSearch로 마이그레이션합니다.
     * 배치 단위로 셀러 ID를 조회하여 Kafka 이벤트를 발행합니다.
     */
    fun migrateAll()
}
