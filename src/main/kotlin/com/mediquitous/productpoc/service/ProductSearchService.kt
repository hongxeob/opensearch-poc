package com.mediquitous.productpoc.service

import com.mediquitous.productpoc.model.dto.CursorPaginationResponse
import com.mediquitous.productpoc.model.dto.SimpleProductDto

/**
 * 상품 검색 서비스 인터페이스
 */
interface ProductSearchService {
    // =====================================================
    // 단건 조회
    // =====================================================

    /**
     * 상품 ID로 단건 조회
     */
    fun getProductById(id: Long): CursorPaginationResponse<SimpleProductDto>

    // =====================================================
    // 여러 상품 조회
    // =====================================================

    /**
     * 여러 상품 ID로 조회 (커서 페이지네이션)
     *
     * @param ids 쉼표로 구분된 상품 ID 목록 (예: "1,2,3")
     * @param ordering 정렬 필드 (예: "-released", "in_stock")
     * @param size 페이지 크기
     * @param cursor 페이지네이션 커서
     */
    fun getProductsByIds(
        ids: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    // =====================================================
    // 기획전/셀러/카테고리별 조회
    // =====================================================

    fun getProductsByDisplayGroup(
        displayGroupId: Long,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    fun getProductsBySeller(
        sellerSlug: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    fun getProductsBySellerType(
        type: String,
        targetGenders: String?,
        styleTags: String?,
        bodyFrameTypes: String?,
        heights: String?,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    fun getProductsByCategory(
        categorySlug: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    fun getProductsByCategoryAndSeller(
        categorySlug: String,
        sellerSlug: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    // =====================================================
    // 검색
    // =====================================================

    fun searchByKeyword(
        keyword: String,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    fun searchByKeywordWithFilters(
        keyword: String,
        sellerType: String?,
        category: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    // =====================================================
    // 홈 탭/랭킹/신상품
    // =====================================================

    fun getProductsByHomeTab(
        tab: String,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    fun getProductsByBestRanking(
        categoryId: Long?,
        managerPart: String?,
        sellerId: Long?,
        period: Int,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    fun getNewestProducts(
        sellerType: String?,
        releasedGte: String?,
        categorySlug: String?,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    // =====================================================
    // 추천
    // =====================================================

    fun getRecommendProducts(
        codes: String,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    fun getProductsByCategoryId(
        categoryId: Long,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    // =====================================================
    // 고객별 조회
    // =====================================================

    fun getLikedProducts(
        customerId: Long,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>

    fun getRecentlyViewedProducts(
        customerId: Long,
        size: Int,
    ): CursorPaginationResponse<SimpleProductDto>

    // =====================================================
    // 기타
    // =====================================================

    fun getProductsByRetailStore(
        name: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto>
}
