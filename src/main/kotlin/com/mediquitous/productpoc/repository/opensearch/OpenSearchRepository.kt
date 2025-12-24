package com.mediquitous.productpoc.repository.opensearch

import com.mediquitous.productpoc.model.dto.SimpleProductDto

/**
 * OpenSearch 리포지토리 인터페이스
 */
interface OpenSearchRepository {
    /**
     * 키워드로 상품 검색
     */
    fun searchByKeyword(
        keyword: String,
        size: Int,
        cursor: String?,
    ): SearchResult

    /**
     * 카테고리 슬러그로 상품 검색
     */
    fun searchByCategorySlug(
        categorySlug: String,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult

    /**
     * 셀러 슬러그로 상품 검색
     */
    fun searchBySellerSlug(
        sellerSlug: String,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult

    /**
     * OpenSearch 검색 결과
     */
    data class SearchResult(
        val totalHits: Long,
        val products: List<SimpleProductDto>,
        val nextCursor: String?,
    )
}
