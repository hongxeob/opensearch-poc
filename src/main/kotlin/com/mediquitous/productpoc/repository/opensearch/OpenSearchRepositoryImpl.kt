@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.mediquitous.productpoc.repository.opensearch

import com.mediquitous.productpoc.model.dto.SimpleProductDto
import com.mediquitous.productpoc.repository.opensearch.query.ProductSearchQueryBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch.core.SearchRequest
import org.opensearch.client.opensearch.core.SearchResponse
import org.springframework.stereotype.Repository
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * OpenSearch 리포지토리 구현체
 */
@Repository
class OpenSearchRepositoryImpl(
    private val openSearchClient: OpenSearchClient,
) : OpenSearchRepository {
    override fun searchByKeyword(
        keyword: String,
        size: Int,
        cursor: String?,
    ): OpenSearchRepository.SearchResult {
        logger.debug { "OpenSearch 키워드 검색: keyword=$keyword, size=$size" }

        // 쿼리 빌드
        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildKeywordSearchQuery(
                keyword = keyword,
                size = size + 1, // hasNext 판단용
                cursor = cursorValues,
            )

        // 검색 실행
        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "OpenSearch 검색 실패: keyword=$keyword" }
                throw OpenSearchException("상품 검색 중 오류가 발생했습니다", e)
            }

        // 응답 파싱
        return parseSearchResponse(response, size)
    }

    override fun searchByCategorySlug(
        categorySlug: String,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): OpenSearchRepository.SearchResult {
        logger.debug { "OpenSearch 카테고리 검색: categorySlug=$categorySlug" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildCategorySlugQuery(
                categorySlug = categorySlug,
                size = size + 1,
                ordering = ordering,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "카테고리 검색 실패: categorySlug=$categorySlug" }
                throw OpenSearchException("카테고리 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchBySellerSlug(
        sellerSlug: String,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): OpenSearchRepository.SearchResult {
        logger.debug { "OpenSearch 셀러 검색: sellerSlug=$sellerSlug" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildSellerSlugQuery(
                sellerSlug = sellerSlug,
                size = size + 1,
                ordering = ordering,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "셀러 검색 실패: sellerSlug=$sellerSlug" }
                throw OpenSearchException("셀러 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchByQuery(build: SearchRequest): OpenSearchRepository.SearchResult {
        TODO("Not yet implemented")
    }

    // ========== Private Helper Functions ==========

    /**
     * OpenSearch 응답을 SearchResult로 변환
     */
    private fun parseSearchResponse(
        response: SearchResponse<Map<*, *>>,
        requestedSize: Int,
    ): OpenSearchRepository.SearchResult {
        val hits = response.hits().hits()
        val totalHits = response.hits().total()?.value() ?: 0L

        // Map -> SimpleProductDto 변환
        val products =
            hits.mapNotNull { hit ->
                val source = hit.source() ?: return@mapNotNull null
                convertToSimpleProductDto(source)
            }

        // 다음 커서 생성
        val nextCursor =
            if (hits.isNotEmpty()) {
                val lastSortValues = hits.last().sort()
                if (lastSortValues != null && lastSortValues.isNotEmpty()) {
                    encodeCursor(lastSortValues)
                } else {
                    null
                }
            } else {
                null
            }

        logger.debug { "검색 완료: totalHits=$totalHits, resultSize=${products.size}" }

        return OpenSearchRepository.SearchResult(
            totalHits = totalHits,
            products = products,
            nextCursor = nextCursor,
        )
    }

    /**
     * OpenSearch 문서(Map)를 SimpleProductDto로 변환
     */
    private fun convertToSimpleProductDto(source: Map<*, *>): SimpleProductDto {
        val seller = source["seller"] as? Map<*, *> ?: emptyMap<String, Any>()

        return SimpleProductDto(
            id = (source["id"] as? Number)?.toLong() ?: 0L,
            code = source["code"] as? String ?: "",
            name = source["name"] as? String ?: "",
            sellerId = (seller["id"] as? Number)?.toLong() ?: 0L,
            sellerName = seller["name"] as? String ?: "",
            sellerSlug = seller["slug"] as? String ?: "",
            price = (source["price"] as? Number)?.toLong() ?: 0L,
            discountPrice = (source["discount_price"] as? Number)?.toLong(),
            discountRate = (source["discount_rate"] as? Number)?.toInt(),
            inStock = source["in_stock"] as? Boolean ?: false,
            thumbnailUrl = source["thumbnail_url"] as? String,
            likeCount = (source["like_count"] as? Number)?.toLong() ?: 0L,
            reviewCount = (source["review_count"] as? Number)?.toLong() ?: 0L,
            reviewScore = (source["review_score"] as? Number)?.toDouble(),
        )
    }

    /**
     * Base64로 커서 인코딩
     *
     * ⚠️ 중요: FieldValue 객체에서 실제 값을 추출해야 함
     */
    private fun encodeCursor(sortValues: List<*>): String {
        val actualValues =
            sortValues.map { value ->
                when (value) {
                    is org.opensearch.client.opensearch._types.FieldValue -> {
                        // FieldValue 객체에서 실제 값 추출
                        when {
                            value.isDouble -> value.doubleValue()
                            value.isLong -> value.longValue()
                            value.isBoolean -> value.booleanValue()
                            value.isString -> value.stringValue()
                            else -> value.toString()
                        }
                    }

                    else -> {
                        value
                    }
                }
            }

        val json =
            actualValues.joinToString(",", "[", "]") {
                when (it) {
                    is String -> "\"$it\""
                    else -> it.toString()
                }
            }

        logger.trace { "커서 인코딩: actualValues=$actualValues, json=$json" }
        return Base64.getEncoder().encodeToString(json.toByteArray())
    }

    /**
     * Base64 커서 디코딩
     *
     * JSON 배열을 파싱하여 String 리스트로 반환
     */
    private fun decodeCursor(cursor: String): List<String>? =
        try {
            val json = String(Base64.getDecoder().decode(cursor))
            logger.trace { "커서 디코딩: json=$json" }

            // 간단한 JSON 배열 파싱
            json
                .trim('[', ']')
                .split(",")
                .map { it.trim().trim('"') }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            logger.warn(e) { "커서 디코딩 실패: cursor=$cursor" }
            null
        }
}

/**
 * OpenSearch 예외
 */
class OpenSearchException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
