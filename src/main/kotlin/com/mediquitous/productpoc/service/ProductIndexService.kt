package com.mediquitous.productpoc.service

/**
 * 상품 인덱스 서비스 인터페이스
 *
 * Go 서버의 product/index_service.go 구조 변환
 * OpenSearch 인덱스에 상품 정보를 CRUD하는 책임
 */
interface ProductIndexService {
    /**
     * 상품 정보를 OpenSearch 인덱스에 업데이트
     *
     * @param productId 상품 ID
     */
    fun updateProduct(productId: Long)

    /**
     * 상품 정보를 OpenSearch 인덱스에서 삭제
     *
     * @param productId 상품 ID
     */
    fun deleteProduct(productId: Long)

    /**
     * 여러 상품 정보를 일괄 업데이트
     *
     * @param productIds 상품 ID 목록
     */
    fun bulkUpdateProducts(productIds: List<Long>)
}
