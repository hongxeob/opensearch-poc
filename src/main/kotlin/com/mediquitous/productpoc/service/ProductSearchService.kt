package com.mediquitous.productpoc.service

import com.mediquitous.productpoc.model.dto.CursorPaginationResponse
import com.mediquitous.productpoc.model.dto.SimpleProductDto

/**
 * 상품 검색 서비스 인터페이스
 */
interface ProductSearchService {
    /**
     * 키워드로 상품 검색
     */
    fun searchByKeyword(
        keyword: String,
        size: Int = 20,
        cursor: String? = null,
    ): CursorPaginationResponse<SimpleProductDto>

    /**
     * 카테고리 슬러그로 상품 검색
     */
    fun searchByCategorySlug(
        categorySlug: String,
        size: Int = 20,
        ordering: String? = null,
        cursor: String? = null,
    ): CursorPaginationResponse<SimpleProductDto>

    /**
     * 셀러 슬러그로 상품 검색
     */
    fun searchBySellerSlug(
        sellerSlug: String,
        size: Int = 20,
        ordering: String? = null,
        cursor: String? = null,
    ): CursorPaginationResponse<SimpleProductDto>
}
