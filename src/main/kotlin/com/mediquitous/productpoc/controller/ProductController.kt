package com.mediquitous.productpoc.controller

import com.mediquitous.productpoc.model.dto.CursorPaginationResponse
import com.mediquitous.productpoc.model.dto.SimpleProductDto
import com.mediquitous.productpoc.service.ProductSearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * 상품 검색 컨트롤러
 */
@Tag(name = "Products", description = "상품 검색 API")
@RestController
@RequestMapping("/api/v1/products")
@Validated
class ProductController(
    private val productSearchService: ProductSearchService,
) {
    @Operation(summary = "키워드로 상품 검색")
    @GetMapping("/search")
    fun searchProductsByKeyword(
        @Parameter(description = "검색 키워드", required = true)
        @RequestParam keyword: String,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100)
        size: Int,
        @Parameter(description = "커서")
        @RequestParam(required = false)
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.searchByKeyword(keyword, size, cursor)

    @Operation(summary = "카테고리 슬러그로 상품 조회")
    @GetMapping("/category")
    fun getProductsByCategorySlug(
        @Parameter(description = "카테고리 슬러그", required = true)
        @RequestParam category: String,
        @Parameter(description = "정렬 필드")
        @RequestParam(required = false)
        ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100)
        size: Int,
        @Parameter(description = "커서")
        @RequestParam(required = false)
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.searchByCategorySlug(category, size, ordering, cursor)

    @Operation(summary = "셀러 슬러그로 상품 조회")
    @GetMapping("/seller")
    fun getProductsBySellerSlug(
        @Parameter(description = "셀러 슬러그", required = true)
        @RequestParam("seller__slug") sellerSlug: String,
        @Parameter(description = "정렬 필드")
        @RequestParam(required = false)
        ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100)
        size: Int,
        @Parameter(description = "커서")
        @RequestParam(required = false)
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.searchBySellerSlug(sellerSlug, size, ordering, cursor)
}
