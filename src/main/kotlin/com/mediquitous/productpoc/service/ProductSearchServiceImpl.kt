package com.mediquitous.productpoc.service

import com.mediquitous.productpoc.model.dto.CursorPaginationResponse
import com.mediquitous.productpoc.model.dto.SimpleProductDto
import com.mediquitous.productpoc.repository.opensearch.OpenSearchRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * 상품 검색 서비스 구현체
 */
@Service
class ProductSearchServiceImpl(
    private val openSearchRepository: OpenSearchRepository,
) : ProductSearchService {
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
        val hasNext = searchResult.products.size > size
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

    override fun searchByCategorySlug(
        categorySlug: String,
        size: Int,
        ordering: String?,
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

        val hasNext = searchResult.products.size > size
        val results = if (hasNext) searchResult.products.take(size) else searchResult.products

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = results,
            nextCursor = if (hasNext) searchResult.nextCursor else null,
            previousCursor = null,
        )
    }

    override fun searchBySellerSlug(
        sellerSlug: String,
        size: Int,
        ordering: String?,
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

        val hasNext = searchResult.products.size > size
        val results = if (hasNext) searchResult.products.take(size) else searchResult.products

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = results,
            nextCursor = if (hasNext) searchResult.nextCursor else null,
            previousCursor = null,
        )
    }
}
