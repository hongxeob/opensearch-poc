package com.mediquitous.productpoc.service

import com.mediquitous.productpoc.model.dto.CursorPaginationResponse
import com.mediquitous.productpoc.model.dto.SimpleProductDto
import com.mediquitous.productpoc.repository.opensearch.OpenSearchRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * 상품 검색 서비스 구현체
 *
 * Go 서버의 product service 계층을 마이그레이션
 */
@Service
class ProductSearchServiceImpl(
    private val openSearchRepository: OpenSearchRepository,
) : ProductSearchService {
    // =====================================================
    // 단건 조회
    // =====================================================

    override fun getProductById(id: Long): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "상품 단건 조회: id=$id" }

        val searchResult =
            openSearchRepository.searchByProductIds(
                productIds = listOf(id),
                size = 1,
                ordering = null,
                cursor = null,
            )

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = searchResult.products,
            nextCursor = null,
            previousCursor = null,
        )
    }

    // =====================================================
    // 여러 상품 조회
    // =====================================================

    override fun getProductsByIds(
        ids: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "상품 ID 목록 조회: ids=$ids, ordering=$ordering, size=$size" }

        val productIds = parseProductIds(ids)
        if (productIds.isEmpty()) {
            logger.warn { "빈 상품 ID 목록" }
            return CursorPaginationResponse.empty()
        }

        val searchResult =
            openSearchRepository.searchByProductIds(
                productIds = productIds,
                size = size,
                ordering = ordering,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    // =====================================================
    // 기획전/셀러/카테고리별 조회
    // =====================================================

    override fun getProductsByDisplayGroup(
        displayGroupId: Long,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "기획전 검색: displayGroupId=$displayGroupId, size=$size" }

        val searchResult =
            openSearchRepository.searchByDisplayGroupId(
                displayGroupId = displayGroupId,
                size = size,
                ordering = ordering,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    override fun getProductsBySeller(
        sellerSlug: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "셀러 검색: sellerSlug=$sellerSlug, size=$size" }

        val searchResult =
            openSearchRepository.searchBySellerSlug(
                sellerSlug = sellerSlug,
                size = size,
                ordering = ordering,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    override fun getProductsBySellerType(
        type: String,
        targetGenders: String?,
        styleTags: String?,
        bodyFrameTypes: String?,
        heights: String?,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "셀러 타입 검색: type=$type, size=$size" }

        // 홈탭 타입 검색으로 대체 (brand, director, beauty 등)
        val searchResult =
            openSearchRepository.searchByHomeTab(
                tabType = type,
                size = size,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    override fun getProductsByCategory(
        categorySlug: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "카테고리 검색: categorySlug=$categorySlug, size=$size" }

        val searchResult =
            openSearchRepository.searchByCategorySlug(
                categorySlug = categorySlug,
                size = size,
                ordering = ordering,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    override fun getProductsByCategoryAndSeller(
        categorySlug: String,
        sellerSlug: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "카테고리+셀러 검색: categorySlug=$categorySlug, sellerSlug=$sellerSlug, size=$size" }

        val searchResult =
            openSearchRepository.searchByCategoryAndSellerSlug(
                categorySlug = categorySlug,
                sellerSlug = sellerSlug,
                size = size,
                ordering = ordering,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    // =====================================================
    // 검색
    // =====================================================

    override fun searchByKeyword(
        keyword: String,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "상품 키워드 검색: keyword=$keyword, size=$size" }

        if (keyword.trim().isEmpty()) {
            logger.warn { "빈 키워드 검색 시도" }
            return CursorPaginationResponse.empty()
        }

        val searchResult =
            openSearchRepository.searchByKeyword(
                keyword = keyword.trim(),
                size = size,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    override fun searchByKeywordWithFilters(
        keyword: String,
        sellerType: String?,
        category: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "키워드+필터 검색: keyword=$keyword, sellerType=$sellerType, category=$category" }

        if (keyword.trim().isEmpty() && sellerType.isNullOrBlank() && category.isNullOrBlank()) {
            logger.warn { "검색 조건이 없습니다" }
            return CursorPaginationResponse.empty()
        }

        val searchResult =
            openSearchRepository.searchByKeywordWithFilters(
                keyword = keyword.trim(),
                sellerType = sellerType,
                categorySlug = category,
                ordering = null,
                size = size,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    // =====================================================
    // 홈 탭/랭킹/신상품
    // =====================================================

    override fun getProductsByHomeTab(
        tab: String,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "홈탭 검색: tab=$tab, size=$size" }

        if (tab.isBlank()) {
            logger.warn { "빈 탭 파라미터" }
            return CursorPaginationResponse.empty()
        }

        val searchResult =
            openSearchRepository.searchByHomeTab(
                tabType = tab,
                size = size,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    override fun getProductsByBestRanking(
        categoryId: Long?,
        managerPart: String?,
        sellerId: Long?,
        period: Int,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "베스트 랭킹 검색: categoryId=$categoryId, period=$period, size=$size" }

        // TODO: PostgreSQL에서 랭킹 spec 조회 후 상품 ID 목록을 가져와서 검색
        // 현재는 OpenSearch만으로는 구현 불가 - DB 연동 필요
        logger.warn { "베스트 랭킹 검색은 DB 연동이 필요합니다" }
        return CursorPaginationResponse.empty()
    }

    override fun getNewestProducts(
        sellerType: String?,
        releasedGte: String?,
        categorySlug: String?,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "신상품 검색: sellerType=$sellerType, categorySlug=$categorySlug, size=$size" }

        val searchResult =
            openSearchRepository.searchNewest(
                sellerType = sellerType,
                releasedGte = releasedGte,
                categorySlug = categorySlug,
                ordering = ordering,
                size = size,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    // =====================================================
    // 추천
    // =====================================================

    override fun getRecommendProducts(
        codes: String,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "추천 상품 검색: codes=$codes, size=$size" }

        val codeList = parseCodes(codes)
        if (codeList.isEmpty()) {
            logger.warn { "빈 추천 상품 코드 목록" }
            return CursorPaginationResponse.empty()
        }

        val searchResult =
            openSearchRepository.searchByRecommendCodes(
                codes = codeList,
                size = size,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    override fun getProductsByCategoryId(
        categoryId: Long,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "카테고리 ID 검색: categoryId=$categoryId, size=$size" }

        val searchResult =
            openSearchRepository.searchByCategoryId(
                categoryId = categoryId,
                size = size,
                ordering = ordering,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    // =====================================================
    // 고객별 조회
    // =====================================================

    override fun getLikedProducts(
        customerId: Long,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "좋아요 상품 검색: customerId=$customerId, size=$size" }

        // TODO: PostgreSQL에서 좋아요 상품 ID 조회 후 OpenSearch 검색
        // 현재는 DB 연동이 필요
        logger.warn { "좋아요 상품 검색은 DB 연동이 필요합니다" }
        return CursorPaginationResponse.empty()
    }

    override fun getRecentlyViewedProducts(
        customerId: Long,
        size: Int,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "최근 본 상품 검색: customerId=$customerId, size=$size" }

        // TODO: PostgreSQL에서 최근 본 상품 ID 조회 후 OpenSearch 검색
        // 현재는 DB 연동이 필요
        logger.warn { "최근 본 상품 검색은 DB 연동이 필요합니다" }
        return CursorPaginationResponse.empty()
    }

    // =====================================================
    // 기타
    // =====================================================

    override fun getProductsByRetailStore(
        name: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "리테일 스토어 검색: name=$name, size=$size" }

        if (name.isBlank()) {
            logger.warn { "빈 리테일 스토어명" }
            return CursorPaginationResponse.empty()
        }

        val searchResult =
            openSearchRepository.searchByRetailStoreName(
                retailStoreName = name,
                size = size,
                ordering = ordering,
                cursor = cursor,
            )

        return buildPaginationResponse(searchResult, size)
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    /**
     * 쉼표로 구분된 상품 ID 문자열을 파싱
     */
    private fun parseProductIds(ids: String): List<Long> =
        ids
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .distinct()

    /**
     * 쉼표로 구분된 상품 코드 문자열을 파싱
     */
    private fun parseCodes(codes: String): List<String> =
        codes
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    /**
     * 검색 결과를 페이지네이션 응답으로 변환
     */
    private fun buildPaginationResponse(
        searchResult: OpenSearchRepository.SearchResult,
        size: Int,
    ): CursorPaginationResponse<SimpleProductDto> {
        val hasNext = searchResult.products.size > size
        val results = if (hasNext) searchResult.products.take(size) else searchResult.products
        val nextCursor = if (hasNext) searchResult.nextCursor else null

        logger.debug { "검색 완료: totalHits=${searchResult.totalHits}, resultSize=${results.size}, hasNext=$hasNext" }

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = results,
            nextCursor = nextCursor,
            previousCursor = null,
        )
    }
}
