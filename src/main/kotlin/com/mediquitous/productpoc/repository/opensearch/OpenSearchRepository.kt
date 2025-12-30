package com.mediquitous.productpoc.repository.opensearch

import com.mediquitous.productpoc.model.document.ProductDocument

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
     * 상품 ID 목록으로 검색
     */
    fun searchByProductIds(
        productIds: List<Long>,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult

    /**
     * 기획전(Display Group) ID로 상품 검색
     */
    fun searchByDisplayGroupId(
        displayGroupId: Long,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult

    /**
     * 카테고리 + 셀러 슬러그로 상품 검색
     */
    fun searchByCategoryAndSellerSlug(
        categorySlug: String,
        sellerSlug: String,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult

    /**
     * 홈탭 타입별 상품 검색
     */
    fun searchByHomeTab(
        tabType: String,
        size: Int,
        cursor: String?,
    ): SearchResult

    /**
     * 신상품 검색
     */
    fun searchNewest(
        sellerType: String?,
        releasedGte: String?,
        categorySlug: String?,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): SearchResult

    /**
     * 추천 상품 코드로 검색
     */
    fun searchByRecommendCodes(
        codes: List<String>,
        size: Int,
        cursor: String?,
    ): SearchResult

    /**
     * 카테고리 ID로 상품 검색
     */
    fun searchByCategoryId(
        categoryId: Long,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult

    /**
     * 리테일 스토어명으로 상품 검색
     */
    fun searchByRetailStoreName(
        retailStoreName: String,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult

    /**
     * 키워드 + 필터(셀러타입, 카테고리) 검색
     */
    fun searchByKeywordWithFilters(
        keyword: String,
        sellerType: String?,
        categorySlug: String?,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): SearchResult

    /**
     * 상품 ID 목록으로 벌크 검색 (정렬 없이)
     */
    fun searchByProductIdsBulk(productIds: List<Long>): SearchResult

    /**
     * OpenSearch 검색 결과
     */
    data class SearchResult(
        val totalHits: Long,
        val documents: List<ProductDocument>,
        val nextCursor: String?,
    )
}
