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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 상품 조회 컨트롤러
 *
 * Go 서버의 handler.go를 마이그레이션한 컨트롤러
 */
@Tag(name = "Products", description = "상품 조회 API")
@RestController
@RequestMapping("/api/v1/products")
@Validated
class ProductQueryController(
    private val productSearchService: ProductSearchService,
) {
    // =====================================================
    // 단건 조회
    // =====================================================

//    @Operation(summary = "상품 ID로 단건 조회", description = "특정 상품의 상세 정보를 조회합니다")
//    @GetMapping("/{id}")
//    fun getProductById(
//        @Parameter(description = "상품 ID", required = true)
//        @PathVariable id: Long,
//    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getProductById(id)

    // =====================================================
    // 여러 상품 조회
    // =====================================================

    @Operation(summary = "여러 상품 ID로 조회", description = "쉼표로 구분된 상품 ID 목록으로 상품을 조회합니다")
    @GetMapping
    fun getProductsByIds(
        @Parameter(description = "쉼표로 구분된 상품 ID 목록", required = true, example = "1,2,3")
        @RequestParam ids: String,
        @Parameter(description = "정렬 필드 (예: -released, in_stock)")
        @RequestParam(required = false) ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getProductsByIds(ids, ordering, size, cursor)

    // =====================================================
    // 기획전/셀러/카테고리별 조회
    // =====================================================

    @Operation(summary = "카테고리별 상품 조회", description = "특정 카테고리의 상품을 조회합니다")
    @GetMapping("/category")
    fun getProductsByCategory(
        @Parameter(description = "카테고리 슬러그", required = true)
        @RequestParam category: String,
        @Parameter(description = "정렬 필드 (예: productbestorder, -released)")
        @RequestParam(required = false) ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getProductsByCategory(category, ordering, size, cursor)

    @Operation(summary = "기획전별 상품 조회", description = "특정 기획전에 포함된 상품을 조회합니다")
    @GetMapping("/display-group")
    fun getProductsByDisplayGroup(
        @Parameter(description = "기획전 ID", required = true)
        @RequestParam id: Long,
        @Parameter(description = "정렬 필드 (예: displaygroupproduct__seq, in_stock)")
        @RequestParam(required = false) ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getProductsByDisplayGroup(id, ordering, size, cursor)

    @Operation(summary = "셀러별 상품 조회", description = "특정 셀러의 상품을 조회합니다")
    @GetMapping("/seller")
    fun getProductsBySeller(
        @Parameter(description = "셀러 슬러그", required = true)
        @RequestParam("seller__slug") sellerSlug: String,
        @Parameter(description = "정렬 필드 (예: -released, in_stock, productbestorder)")
        @RequestParam(required = false) ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getProductsBySeller(sellerSlug, ordering, size, cursor)

    @Operation(summary = "셀러 타입별 상품 조회", description = "셀러 타입으로 필터링된 상품을 조회합니다")
    @GetMapping("/seller-type")
    fun getProductsBySellerType(
        @Parameter(description = "셀러 타입 (예: director, trend_shppingmall, k_brand, j_brand, beauty)", required = true)
        @RequestParam type: String,
        @Parameter(description = "타겟 성별 (쉼표 구분, 예: female,male)")
        @RequestParam(name = "target_genders", required = false) targetGenders: String?,
        @Parameter(description = "스타일 태그 (쉼표 구분)")
        @RequestParam(name = "style_tags", required = false) styleTags: String?,
        @Parameter(description = "체형 타입 (쉼표 구분)")
        @RequestParam(name = "body_frame_types", required = false) bodyFrameTypes: String?,
        @Parameter(description = "키 범위 (예: 170~174,175~179)")
        @RequestParam(required = false) heights: String?,
        @Parameter(description = "정렬 필드 (예: -order_amount,-updated, -open_at,-created, brand_name)")
        @RequestParam(required = false) ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> =
        productSearchService.getProductsBySellerType(
            type,
            targetGenders,
            styleTags,
            bodyFrameTypes,
            heights,
            ordering,
            size,
            cursor,
        )

    @Operation(summary = "카테고리+셀러별 상품 조회", description = "특정 카테고리와 셀러의 상품을 조회합니다")
    @GetMapping("/category/seller")
    fun getProductsByCategoryAndSeller(
        @Parameter(description = "카테고리 슬러그", required = true)
        @RequestParam category: String,
        @Parameter(description = "셀러 슬러그", required = true)
        @RequestParam("seller__slug") sellerSlug: String,
        @Parameter(description = "정렬 필드 (예: in_stock, -released)")
        @RequestParam(required = false) ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> =
        productSearchService.getProductsByCategoryAndSeller(category, sellerSlug, ordering, size, cursor)

    // =====================================================
    // 검색
    // =====================================================

    @Operation(summary = "키워드로 상품 검색", description = "OpenSearch를 사용한 키워드 검색")
    @GetMapping("/opensearch/products")
    fun searchProductsByKeyword(
        @Parameter(description = "검색 키워드", required = true)
        @RequestParam keyword: String,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.searchByKeyword(keyword, size, cursor)

    @Operation(
        summary = "키워드+셀러타입+카테고리로 상품 검색",
        description = "OpenSearch를 사용한 필터링 조합 검색 (V2)",
    )
    @GetMapping("/opensearch/products/v2")
    fun searchProductsByKeywordWithFilters(
        @Parameter(description = "검색 키워드", required = true)
        @RequestParam keyword: String,
        @Parameter(description = "셀러 타입")
        @RequestParam(name = "seller_type", required = false) sellerType: String?,
        @Parameter(description = "카테고리")
        @RequestParam(required = false) category: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> =
        productSearchService.searchByKeywordWithFilters(keyword, sellerType, category, size, cursor)

    // =====================================================
    // 홈 탭/랭킹/신상품
    // =====================================================

    @Operation(summary = "홈 탭별 상품 조회", description = "인기순 정렬로 홈 탭별 상품을 조회합니다")
    @GetMapping("/tab")
    fun getProductsByHomeTab(
        @Parameter(description = "홈 탭 타입", required = true)
        @RequestParam tab: String,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getProductsByHomeTab(tab, size, cursor)

    @Operation(summary = "베스트 랭킹 상품 조회", description = "기간별 베스트 랭킹 상품을 조회합니다")
    @GetMapping("/best-ranking")
    fun getProductsByBestRanking(
        @Parameter(description = "카테고리 ID")
        @RequestParam(name = "category_id", required = false) categoryId: Long?,
        @Parameter(description = "담당 파트")
        @RequestParam(name = "manager_part", required = false) managerPart: String?,
        @Parameter(description = "셀러 ID")
        @RequestParam(name = "seller_id", required = false) sellerId: Long?,
        @Parameter(description = "기간 (1: 일간, 7: 주간, 30: 월간)", required = true)
        @RequestParam period: Int,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> =
        productSearchService.getProductsByBestRanking(categoryId, managerPart, sellerId, period, size, cursor)

    @Operation(summary = "신상품 조회", description = "최신 출시된 상품을 조회합니다")
    @GetMapping("/newest")
    fun getNewestProducts(
        @Parameter(description = "셀러 타입")
        @RequestParam(name = "seller__type", required = false) sellerType: String?,
        @Parameter(description = "출시일 이후 (YYYY-MM-DD)")
        @RequestParam(name = "released__gte", required = false) releasedGte: String?,
        @Parameter(description = "카테고리 슬러그")
        @RequestParam(name = "category_set__slug__all", required = false) categorySlug: String?,
        @Parameter(description = "정렬 필드 (예: -released, productbestorder, in_stock)")
        @RequestParam(required = false) ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> =
        productSearchService.getNewestProducts(sellerType, releasedGte, categorySlug, ordering, size, cursor)

    // =====================================================
    // 추천
    // =====================================================

    @Operation(summary = "실시간 추천 상품 조회", description = "상품 코드 목록으로 추천 상품을 조회합니다")
    @GetMapping("/realtime")
    fun getRecommendProducts(
        @Parameter(description = "상품 코드 (쉼표 구분)", required = true)
        @RequestParam codes: String,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getRecommendProducts(codes, size, cursor)

    @Operation(summary = "카테고리 ID 기반 추천 상품", description = "특정 카테고리 ID의 추천 상품을 조회합니다")
    @GetMapping("/recommend/category")
    fun getProductsByCategoryId(
        @Parameter(description = "카테고리 ID", required = true)
        @RequestParam id: Long,
        @Parameter(description = "정렬 필드 (예: -released, -sales_amount)")
        @RequestParam(required = false) ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "10")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getProductsByCategoryId(id, ordering, size, cursor)

    // =====================================================
    // 고객별 조회
    // =====================================================

    @Operation(summary = "좋아요한 상품 조회", description = "고객이 좋아요한 상품 목록을 조회합니다")
    @GetMapping("/liked")
    fun getLikedProducts(
        @Parameter(description = "고객 ID (헤더)", required = true)
        @RequestHeader("X-Customer-ID") customerId: Long,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getLikedProducts(customerId, size, cursor)

    @Operation(summary = "최근 본 상품 조회", description = "고객이 최근 조회한 상품 목록을 조회합니다")
    @GetMapping("/recently-viewed")
    fun getRecentlyViewedProducts(
        @Parameter(description = "고객 ID (헤더)", required = true)
        @RequestHeader("X-Customer-ID") customerId: Long,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getRecentlyViewedProducts(customerId, size)

    // =====================================================
    // 기타
    // =====================================================

    @Operation(summary = "리테일 스토어별 상품 조회", description = "특정 리테일 스토어의 상품을 조회합니다")
    @GetMapping("/retail-store")
    fun getProductsByRetailStore(
        @Parameter(description = "리테일 스토어 이름", required = true)
        @RequestParam name: String,
        @Parameter(description = "정렬 필드")
        @RequestParam(required = false) ordering: String?,
        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(100) size: Int,
        @Parameter(description = "페이지네이션 커서")
        @RequestParam(required = false) cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> = productSearchService.getProductsByRetailStore(name, ordering, size, cursor)
}
