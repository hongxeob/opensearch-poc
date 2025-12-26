package com.mediquitous.productpoc.service

import com.mediquitous.productpoc.model.dto.CursorPaginationResponse
import com.mediquitous.productpoc.model.dto.SimpleProductDto
import com.mediquitous.productpoc.repository.opensearch.OpenSearchRepository
import com.mediquitous.productpoc.repository.opensearch.query.ProductSearchQueryBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.Base64

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
        TODO("Not yet implemented")
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

        // 1. 상품 ID 파싱
        val productIds = parseProductIds(ids)
        if (productIds.isEmpty()) {
            logger.warn { "빈 상품 ID 목록" }
            return CursorPaginationResponse.empty()
        }

        // 2. 커서 디코딩 (search_after 값 추출)
        val searchAfter = cursor?.let { decodeSearchAfter(it) }

        // 3. OpenSearch 쿼리 생성
        val searchRequest =
            ProductSearchQueryBuilder.buildProductIdsQuery(
                productIds = productIds,
                size = size + 1, // 다음 페이지 존재 여부 확인을 위해 +1
                ordering = ordering,
                cursor = searchAfter?.map { it.toString() },
            )

        // 4. OpenSearch 검색 실행
        val searchResult = openSearchRepository.searchByQuery(searchRequest)

        // 5. 페이지네이션 응답 생성
        val hasNext = checkHasNext(searchResult, size)
        val results =
            if (hasNext) {
                searchResult.products.take(size)
            } else {
                searchResult.products
            }

        // 6. 다음 커서 생성
        val nextCursor =
            if (hasNext) {
                searchResult.nextCursor
            } else {
                null
            }

        logger.info { "조회 완료: totalHits=${searchResult.totalHits}, resultSize=${results.size}, hasNext=$hasNext" }

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = results,
            nextCursor = nextCursor,
            previousCursor = null,
        )
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
        TODO("Not yet implemented")
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

        val hasNext = checkHasNext(searchResult, size)
        val results = if (hasNext) searchResult.products.take(size) else searchResult.products

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = results,
            nextCursor = if (hasNext) searchResult.nextCursor else null,
            previousCursor = null,
        )
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
        TODO("Not yet implemented")
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

        val hasNext = checkHasNext(searchResult, size)
        val results = if (hasNext) searchResult.products.take(size) else searchResult.products

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = results,
            nextCursor = if (hasNext) searchResult.nextCursor else null,
            previousCursor = null,
        )
    }

    override fun getProductsByCategoryAndSeller(
        categorySlug: String,
        sellerSlug: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
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

        // 빈 키워드 처리
        if (keyword.trim().isEmpty()) {
            logger.warn { "빈 키워드 검색 시도" }
            return CursorPaginationResponse.empty()
        }

        // OpenSearch 검색
        val searchResult =
            openSearchRepository.searchByKeyword(
                keyword = keyword.trim(),
                size = size,
                cursor = cursor,
            )

        // 페이지네이션 응답 생성
        val hasNext = checkHasNext(searchResult, size)
        val results =
            if (hasNext) {
                searchResult.products.take(size)
            } else {
                searchResult.products
            }

        val nextCursor =
            if (hasNext) {
                searchResult.nextCursor
            } else {
                null
            }

        logger.info { "검색 완료: totalHits=${searchResult.totalHits}, resultSize=${results.size}" }

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = results,
            nextCursor = nextCursor,
            previousCursor = null,
        )
    }

    override fun searchByKeywordWithFilters(
        keyword: String,
        sellerType: String?,
        category: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
    }

    // =====================================================

    override fun getProductsByHomeTab(
        tab: String,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
    }

    override fun getProductsByBestRanking(
        categoryId: Long?,
        managerPart: String?,
        sellerId: Long?,
        period: Int,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
    }

    override fun getNewestProducts(
        sellerType: String?,
        releasedGte: String?,
        categorySlug: String?,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
    }

    // =====================================================

    override fun getRecommendProducts(
        codes: String,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
    }

    override fun getProductsByCategoryId(
        categoryId: Long,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
    }

    // =====================================================

    override fun getLikedProducts(
        customerId: Long,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
    }

    override fun getRecentlyViewedProducts(
        customerId: Long,
        size: Int,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
    }

    // =====================================================

    override fun getProductsByRetailStore(
        name: String,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        TODO("Not yet implemented")
    }

    // =====================================================
    // 기타
    // =====================================================
    // 고객별 조회
    // =====================================================
    // 추천
    // =====================================================
    // 홈 탭/랭킹/신상품
    private fun checkHasNext(
        searchResult: OpenSearchRepository.SearchResult,
        size: Int,
    ): Boolean {
        val hasNext = searchResult.products.size > size
        return hasNext
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    /**
     * 쉼표로 구분된 상품 ID 문자열을 파싱
     *
     * @param ids 예: "1,2,3,4,5"
     * @return List<Long>
     */
    private fun parseProductIds(ids: String): List<Long> =
        ids
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .distinct()

    /**
     * search_after 커서 디코딩
     *
     * OpenSearch의 search_after는 정렬 값들의 배열
     * 예: Base64("[\"2024-01-15\", 123]") → listOf("2024-01-15", 123)
     */
    private fun decodeSearchAfter(cursor: String): List<Any>? =
        try {
            val decoded = String(Base64.getDecoder().decode(cursor))
            // JSON 파싱 필요 (간단히 구현)
            // TODO: Jackson ObjectMapper로 파싱
            logger.debug { "커서 디코딩: $decoded" }
            null // 임시로 null 반환
        } catch (e: Exception) {
            logger.warn(e) { "커서 디코딩 실패: $cursor" }
            null
        }

    /**
     * search_after 값을 커서로 인코딩
     *
     * @param sortValues OpenSearch sort 값
     * @return Base64 encoded cursor
     */
    private fun encodeSearchAfter(sortValues: List<Any>?): String? =
        sortValues?.let {
            try {
                // JSON 직렬화 필요
                // TODO: Jackson ObjectMapper로 직렬화
                val json = sortValues.toString() // 임시 구현
                Base64.getEncoder().encodeToString(json.toByteArray())
            } catch (e: Exception) {
                logger.warn(e) { "커서 인코딩 실패" }
                null
            }
        }
}
