package com.mediquitous.productpoc.repository.opensearch.query

import org.opensearch.client.opensearch._types.FieldValue
import org.opensearch.client.opensearch._types.SortOrder
import org.opensearch.client.opensearch._types.mapping.FieldType
import org.opensearch.client.opensearch._types.query_dsl.Query
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType
import org.opensearch.client.opensearch.core.SearchRequest

/**
 * 상품 검색 쿼리 빌더 (유틸리티)
 *
 * 지원하는 검색 유형:
 * - 키워드 검색 (buildKeywordSearchQuery)
 * - 카테고리 슬러그 검색 (buildCategorySlugQuery)
 * - 셀러 슬러그 검색 (buildSellerSlugQuery)
 * - 상품 ID 목록 검색 (buildProductIdsQuery)
 */
object ProductSearchQueryBuilder {
    private const val MAX_KEYWORD_LENGTH = 120
    private const val PRODUCTS_INDEX = "zelda-products"
    private const val DEFAULT_ORDERING = "-released"

    /**
     * 키워드 검색 쿼리 생성
     */
    fun buildKeywordSearchQuery(
        keyword: String,
        size: Int,
        cursor: List<String>? = null,
    ): SearchRequest {
        val trimmedKeyword = keyword.trim().take(MAX_KEYWORD_LENGTH)

        return SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query(buildKeywordQuery(trimmedKeyword))
            .size(size)
            .sort { s -> s.score { it.order(SortOrder.Desc) } }
            .sort { s -> s.field { it.field("id").order(SortOrder.Asc) } }
            .apply {
                cursor?.let {
                    searchAfter(it.map { v -> FieldValue.of(v) })
                }
            }.build()
    }

    /**
     * 카테고리 슬러그 검색 쿼리
     */
    fun buildCategorySlugQuery(
        categorySlug: String,
        size: Int,
        ordering: String? = null,
        cursor: List<String>? = null,
    ): SearchRequest =
        SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query { q ->
                q.bool { b ->
                    b
                        .filter { f ->
                            f.nested { n ->
                                n
                                    .path("categories")
                                    .query { cq ->
                                        cq.term { t ->
                                            t
                                                .field("categories.slug")
                                                .value(FieldValue.of(categorySlug))
                                        }
                                    }
                            }
                        }.filter(buildCommonFilters())
                }
            }.size(size)
            .apply {
                addSorting(ordering, cursor)
            }.build()

    /**
     * 셀러 슬러그 검색 쿼리
     */
    fun buildSellerSlugQuery(
        sellerSlug: String,
        size: Int,
        ordering: String? = null,
        cursor: List<String>? = null,
    ): SearchRequest =
        SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query { q ->
                q.bool { b ->
                    b
                        .filter { f ->
                            f.nested { n ->
                                n
                                    .path("seller")
                                    .query { sq ->
                                        sq.term { t ->
                                            t
                                                .field("seller.slug")
                                                .value(FieldValue.of(sellerSlug))
                                        }
                                    }
                            }
                        }.filter(buildCommonFilters())
                }
            }.size(size)
            .apply {
                addSorting(ordering, cursor)
            }.build()

    /**
     * 상품 ID 목록 검색 쿼리
     *
     * Go 서버의 by_ids_with_cursor.go 로직을 Kotlin으로 변환
     *
     * 기본 쿼리 구조:
     * ```json
     * {
     *   "query": {
     *     "bool": {
     *       "filter": [
     *         { "terms": { "id": [1,2,3] } },
     *         { "exists": { "field": "display" } },
     *         { "exists": { "field": "selling" } }
     *       ],
     *       "must_not": [
     *         { "exists": { "field": "deleted" } }
     *       ]
     *     }
     *   },
     *   "size": 21,
     *   "sort": [...],
     *   "search_after": [...]
     * }
     * ```
     */
    fun buildProductIdsQuery(
        productIds: List<Long>,
        size: Int,
        ordering: String? = null,
        cursor: List<String>? = null,
    ): SearchRequest =
        SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query { q ->
                q.bool { b ->
                    b
                        .filter(buildProductIdsFilter(productIds))
                        .filter(buildDisplayAndSellingFilters())
                        .mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }
            }.size(size)
            .apply {
                addSorting(ordering ?: DEFAULT_ORDERING, cursor)
            }.build()

    // ========== Private Helper Functions ==========

    /**
     * 키워드 검색용 복합 쿼리
     */
    private fun buildKeywordQuery(keyword: String): Query =
        Query
            .Builder()
            .bool { b ->
                b
                    .must { m ->
                        m.bool { sb ->
                            sb
                                .should(buildProductNameQuery(keyword))
                                .should(buildProductNameNgramQuery(keyword))
                                .should(buildDescriptionQuery(keyword))
                                .should(buildBarcodeQuery(keyword))
                                .should(buildCodeQuery(keyword))
                                .should(buildSellerQuery(keyword))
                                .should(buildCategoryQuery(keyword))
                                .should(buildOptionQuery(keyword))
                                .should(buildVariantBarcodeQuery(keyword))
                                .minimumShouldMatch("1")
                        }
                    }.filter(buildCommonFilters())
            }.build()

    private fun buildProductNameQuery(keyword: String): Query =
        Query
            .Builder()
            .multiMatch { m ->
                m
                    .query(keyword)
                    .fields("name.ko^3", "name.ja^3", "name.en^3", "name^2", "english_name^2")
                    .type(TextQueryType.BestFields)
            }.build()

    private fun buildProductNameNgramQuery(keyword: String): Query =
        Query
            .Builder()
            .multiMatch { m ->
                m
                    .query(keyword)
                    .fields("name.ko_ngram^2", "name.ja_ngram^2", "name.ngram^2", "english_name.ngram^2")
                    .type(TextQueryType.BestFields)
            }.build()

    private fun buildDescriptionQuery(keyword: String): Query =
        Query
            .Builder()
            .multiMatch { m ->
                m
                    .query(keyword)
                    .fields("description.ko", "description.ja", "description.en")
                    .type(TextQueryType.BestFields)
            }.build()

    private fun buildBarcodeQuery(keyword: String): Query =
        Query
            .Builder()
            .term { t ->
                t
                    .field("barcode")
                    .value(FieldValue.of(keyword))
            }.build()

    private fun buildCodeQuery(keyword: String): Query =
        Query
            .Builder()
            .term { t ->
                t
                    .field("code")
                    .value(FieldValue.of(keyword))
            }.build()

    private fun buildSellerQuery(keyword: String): Query =
        Query
            .Builder()
            .nested { n ->
                n
                    .path("seller")
                    .query { q ->
                        q.multiMatch { m ->
                            m
                                .query(keyword)
                                .fields(
                                    "seller.name.ko^2",
                                    "seller.name.ja^2",
                                    "seller.brand_name.ko^2",
                                    "seller.brand_name.ko_ngram^1",
                                    "seller.name.ko_ngram^1",
                                    "seller.influencer_name.ko^1.5",
                                ).type(TextQueryType.BestFields)
                        }
                    }
            }.build()

    private fun buildCategoryQuery(keyword: String): Query =
        Query
            .Builder()
            .nested { n ->
                n
                    .path("categories")
                    .query { q ->
                        q.multiMatch { m ->
                            m
                                .query(keyword)
                                .fields(
                                    "categories.name.ko^1.5",
                                    "categories.display_name.ko^1.5",
                                    "categories.name.ko_ngram^1",
                                ).type(TextQueryType.BestFields)
                        }
                    }
            }.build()

    private fun buildOptionQuery(keyword: String): Query =
        Query
            .Builder()
            .nested { n ->
                n
                    .path("options")
                    .query { q ->
                        q.multiMatch { m ->
                            m
                                .query(keyword)
                                .fields(
                                    "options.name.ko^1.2",
                                    "options.value.ko^1.2",
                                    "options.search_name.ko^1.3",
                                ).type(TextQueryType.BestFields)
                        }
                    }
            }.build()

    private fun buildVariantBarcodeQuery(keyword: String): Query =
        Query
            .Builder()
            .nested { n ->
                n
                    .path("variants")
                    .query { q ->
                        q.term { t ->
                            t
                                .field("variants.barcode")
                                .value(FieldValue.of(keyword))
                        }
                    }
            }.build()

    /**
     * 공통 필터 (display 존재 & deleted 제외)
     */
    private fun buildCommonFilters(): List<Query> =
        listOf(
            Query.Builder().exists { e -> e.field("display") }.build(),
            Query
                .Builder()
                .bool { b ->
                    b.mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }.build(),
        )

    /**
     * 상품 ID 목록 필터 (terms query)
     */
    private fun buildProductIdsFilter(productIds: List<Long>): Query =
        Query
            .Builder()
            .terms { t ->
                t
                    .field("id")
                    .terms { tf ->
                        tf.value(productIds.map { FieldValue.of(it) })
                    }
            }.build()

    /**
     * display & selling 필드 존재 필터
     */
    private fun buildDisplayAndSellingFilters(): List<Query> =
        listOf(
            Query.Builder().exists { e -> e.field("display") }.build(),
            Query.Builder().exists { e -> e.field("selling") }.build(),
        )

    /**
     * 정렬 추가
     *
     * ⚠️ 중요: Text 필드는 .keyword 서브필드로 정렬해야 함
     * OpenSearch에서 text 필드는 analyzed되어 정렬/집계 불가
     */
    private fun SearchRequest.Builder.addSorting(
        ordering: String?,
        cursor: List<String>?,
    ): SearchRequest.Builder {
        val sortFields = ordering?.split(",")?.map { it.trim() } ?: listOf("-sales_amount")

        sortFields.forEach { field ->
            val isDescending = field.startsWith("-")
            val rawFieldName = if (isDescending) field.substring(1) else field
            val order = if (isDescending) SortOrder.Desc else SortOrder.Asc

            // 필드 타입에 따라 적절한 필드명 선택
            val sortFieldName = getSortableFieldName(rawFieldName)

            sort { s ->
                s.field { f ->
                    f
                        .field(sortFieldName)
                        .order(order)
                        .unmappedType(FieldType.Long) // 필드가 없어도 에러 안나게
                }
            }
        }

        // ID 정렬 (tie-breaker)
        sort { s -> s.field { f -> f.field("id").order(SortOrder.Asc) } }

        // 커서 적용
        cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }

        return this
    }

    /**
     * 정렬 가능한 필드명 반환
     *
     * - 숫자/날짜/boolean 필드: 그대로 사용
     * - text 필드: .keyword 서브필드 사용
     */
    private fun getSortableFieldName(fieldName: String): String =
        when (fieldName) {
            // 숫자 필드 (그대로 사용)
            "id", "price", "discount_price", "discount_rate",
            "sales_amount", "order_count", "view_count", "like_count",
            "review_count", "review_score",
            -> fieldName

            // 날짜 필드 (그대로 사용)
            "released", "created_at", "updated_at", "open_at" -> fieldName

            // Boolean 필드 (그대로 사용)
            "in_stock", "display", "is_active" -> fieldName

            // 특수 필드 (productbestorder)
            "productbestorder" -> fieldName

            // 기타 text 필드 → .keyword 추가
            else -> "$fieldName.keyword"
        }
}
