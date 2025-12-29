package com.mediquitous.productpoc.service.index

/**
 * 셀러 인덱스 서비스 인터페이스
 *
 * Go 서버의 seller/index_service.go 구조 변환
 * OpenSearch 인덱스에 셀러 정보를 CRUD하는 책임
 */
interface SellerIndexService {
    /**
     * 셀러 정보를 OpenSearch 인덱스에 업데이트
     *
     * @param sellerId 셀러 ID
     */
    fun updateSeller(sellerId: Long)

    /**
     * 셀러 정보를 OpenSearch 인덱스에서 삭제
     *
     * @param sellerId 셀러 ID
     */
    fun deleteSeller(sellerId: Long)

    /**
     * 여러 셀러 정보를 일괄 업데이트
     *
     * @param sellerIds 셀러 ID 목록
     */
    fun bulkUpdateSellers(sellerIds: List<Long>)
}
