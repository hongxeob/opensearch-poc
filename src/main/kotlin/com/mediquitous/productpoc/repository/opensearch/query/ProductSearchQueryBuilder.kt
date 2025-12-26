package com.mediquitous.productpoc.repository.opensearch.query

import org.opensearch.client.json.JsonData
import org.opensearch.client.opensearch._types.FieldValue
import org.opensearch.client.opensearch._types.SortOrder
import org.opensearch.client.opensearch._types.mapping.FieldType
import org.opensearch.client.opensearch._types.query_dsl.Query
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType
import org.opensearch.client.opensearch.core.SearchRequest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 상품 검색 쿼리 빌더 (유틸리티)
 *
 * 지원하는 검색 유형:
 * - 키워드 검색 (buildKeywordSearchQuery)
 * - 카테고리 슬러그 검색 (buildCategorySlugQuery)
 * - 셀러 슬러그 검색 (buildSellerSlugQuery)
 * - 상품 ID 목록 검색 (buildProductIdsQuery)
 * - 기획전 검색 (buildDisplayGroupQuery)
 * - 카테고리+셀러 검색 (buildCategoryAndSellerSlugQuery)
 * - 홈탭 검색 (buildHomeTabQuery)
 * - 신상품 검색 (buildNewestQuery)
 * - 추천 상품 검색 (buildRecommendByCodesQuery)
 * - 카테고리 ID 검색 (buildCategoryIdQuery)
 * - 리테일 스토어 검색 (buildRetailStoreQuery)
 * - 키워드+필터 검색 (buildKeywordWithFiltersQuery)
 */
object ProductSearchQueryBuilder {
    private const val MAX_KEYWORD_LENGTH = 120
    private const val PRODUCTS_INDEX = "zelda-products"
    private const val DEFAULT_ORDERING = "-released"

    // Ordering Keys
    private const val ORDERING_IN_STOCK = "in_stock"
    private const val ORDERING_RELEASED = "released"
    private const val ORDERING_PRODUCT_BEST_ORDER = "productbestorder"
    private const val ORDERING_DISPLAY_GROUP_PRODUCT_SEQ = "displaygroupproduct__seq"
    private const val ORDERING_SALES_AMOUNT = "sales_amount"

    // Home Tab Types
    enum class HomeTabType(
        val sellerTypes: List<String>? = null,
        val categorySlug: String? = null,
    ) {
        BRAND(sellerTypes = listOf("k_brand", "j_brand")),
        DIRECTOR(sellerTypes = listOf("director", "trend_shoppingmall")),
        BEAUTY(categorySlug = "beauty"),
    }

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

    // ========== 추가 쿼리 빌더 메서드들 ==========

    /**
     * 기획전(Display Group) 검색 쿼리
     *
     * Go 서버의 by_display_group_id.go 로직 변환
     */
    fun buildDisplayGroupQuery(
        displayGroupId: Long,
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
                        .must { m ->
                            m.nested { n ->
                                n
                                    .path("display_group")
                                    .query { dq ->
                                        dq.term { t ->
                                            t
                                                .field("display_group.id")
                                                .value(FieldValue.of(displayGroupId))
                                        }
                                    }
                            }
                        }.filter { f -> f.exists { e -> e.field("display") } }
                        .mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }
            }.size(size)
            .apply {
                addDisplayGroupSorting(ordering, displayGroupId, cursor)
            }.build()

    /**
     * 카테고리 + 셀러 슬러그 검색 쿼리
     *
     * Go 서버의 by_category_slug_seller_slug.go 로직 변환
     */
    fun buildCategoryAndSellerSlugQuery(
        categorySlug: String,
        sellerSlug: String,
        size: Int,
        ordering: String? = null,
        cursor: List<String>? = null,
    ): SearchRequest {
        val shouldFilterSelling = !containsInStockOrdering(ordering)

        return SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query { q ->
                q.bool { b ->
                    b
                        .must { m ->
                            m.nested { n ->
                                n
                                    .path("categories")
                                    .query { cq ->
                                        cq.term { t ->
                                            t
                                                .field("categories.slug.lc")
                                                .value(FieldValue.of(categorySlug.lowercase()))
                                        }
                                    }
                            }
                        }.must { m ->
                            m.nested { n ->
                                n
                                    .path("seller")
                                    .query { sq ->
                                        sq.term { t ->
                                            t
                                                .field("seller.slug.lc")
                                                .value(FieldValue.of(sellerSlug.lowercase()))
                                        }
                                    }
                            }
                        }.filter { f -> f.exists { e -> e.field("display") } }
                        .apply {
                            if (shouldFilterSelling) {
                                filter { f -> f.exists { e -> e.field("selling") } }
                            }
                        }.mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }
            }.size(size)
            .apply {
                addCategorySellerSorting(ordering, cursor)
            }.build()
    }

    /**
     * 홈탭 타입별 검색 쿼리
     *
     * Go 서버의 by_home_tab_type.go 로직 변환
     * - brand: k_brand, j_brand 셀러
     * - director: director, trend_shoppingmall 셀러
     * - beauty: beauty 카테고리
     */
    fun buildHomeTabQuery(
        tabType: String,
        size: Int,
        cursor: List<String>? = null,
    ): SearchRequest {
        val homeTab =
            HomeTabType.entries.find { it.name.equals(tabType, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unsupported home tab type: $tabType")

        return SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query { q ->
                q.bool { b ->
                    b
                        .must { m ->
                            when {
                                homeTab.sellerTypes != null -> {
                                    m.nested { n ->
                                        n
                                            .path("seller")
                                            .query { sq ->
                                                sq.terms { t ->
                                                    t
                                                        .field("seller.type")
                                                        .terms { tf ->
                                                            tf.value(homeTab.sellerTypes.map { FieldValue.of(it) })
                                                        }
                                                }
                                            }
                                    }
                                }

                                homeTab.categorySlug != null -> {
                                    m.nested { n ->
                                        n
                                            .path("categories")
                                            .query { cq ->
                                                cq.term { t ->
                                                    t
                                                        .field("categories.slug.lc")
                                                        .value(FieldValue.of(homeTab.categorySlug))
                                                }
                                            }
                                    }
                                }

                                else -> {
                                    m
                                }
                            }
                        }.filter { f -> f.exists { e -> e.field("display") } }
                        .filter { f -> f.exists { e -> e.field("selling") } }
                        .mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }
            }.size(size)
            .sort { s ->
                s.field { f ->
                    f
                        .field("best_order.sales_amount")
                        .order(SortOrder.Desc)
                        .missing { mv -> mv.stringValue("_last") }
                }
            }.sort { s -> s.field { f -> f.field("id").order(SortOrder.Asc) } }
            .apply {
                cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }
            }.build()
    }

    /**
     * 신상품 검색 쿼리
     *
     * Go 서버의 by_newest.go 로직 변환
     */
    fun buildNewestQuery(
        sellerType: String? = null,
        releasedGte: String? = null,
        categorySlug: String? = null,
        ordering: String? = null,
        size: Int,
        cursor: List<String>? = null,
    ): SearchRequest {
        val defaultReleasedGte =
            releasedGte ?: LocalDate.now().minusMonths(3).format(DateTimeFormatter.ISO_DATE)

        val categorySlugs =
            categorySlug
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

        return SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query { q ->
                q.bool { b ->
                    b
                        .must { m ->
                            m.range { r ->
                                r
                                    .field("released")
                                    .gte(JsonData.of(defaultReleasedGte))
                            }
                        }.apply {
                            // 카테고리 필터 추가
                            categorySlugs.forEach { slug ->
                                must { m ->
                                    m.nested { n ->
                                        n
                                            .path("categories")
                                            .query { cq ->
                                                cq.term { t ->
                                                    t
                                                        .field("categories.slug.lc")
                                                        .value(FieldValue.of(slug))
                                                }
                                            }
                                    }
                                }
                            }

                            // 셀러 타입 필터 추가
                            if (!sellerType.isNullOrBlank()) {
                                must { m ->
                                    m.nested { n ->
                                        n
                                            .path("seller")
                                            .query { sq ->
                                                sq.term { t ->
                                                    t
                                                        .field("seller.type")
                                                        .value(FieldValue.of(sellerType))
                                                }
                                            }
                                    }
                                }
                            }
                        }.filter { f -> f.exists { e -> e.field("selling") } }
                        .filter { f -> f.exists { e -> e.field("display") } }
                        .mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }
            }.size(size)
            .apply {
                addNewestSorting(ordering, cursor)
            }.build()
    }

    /**
     * 추천 상품 코드 검색 쿼리
     *
     * Go 서버의 recommend_by_codes.go 로직 변환
     */
    fun buildRecommendByCodesQuery(
        codes: List<String>,
        size: Int,
        cursor: List<String>? = null,
    ): SearchRequest =
        SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query { q ->
                q.bool { b ->
                    b
                        .must { m -> m.exists { e -> e.field("selling") } }
                        .must { m ->
                            m.terms { t ->
                                t
                                    .field("code")
                                    .terms { tf ->
                                        tf.value(codes.map { FieldValue.of(it) })
                                    }
                            }
                        }.mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }
            }.size(size)
            .sort { s -> s.field { f -> f.field("id").order(SortOrder.Asc) } }
            .apply {
                cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }
            }.build()

    /**
     * 카테고리 ID 검색 쿼리
     *
     * Go 서버의 by_category_id.go 로직 변환
     */
    fun buildCategoryIdQuery(
        categoryId: Long,
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
                        .must { m ->
                            m.nested { n ->
                                n
                                    .path("categories")
                                    .query { cq ->
                                        cq.term { t ->
                                            t
                                                .field("categories.id")
                                                .value(FieldValue.of(categoryId))
                                        }
                                    }
                            }
                        }.filter { f -> f.exists { e -> e.field("display") } }
                        .filter { f -> f.exists { e -> e.field("selling") } }
                        .mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }
            }.size(size)
            .apply {
                addCategoryIdSorting(ordering, cursor)
            }.build()

    /**
     * 리테일 스토어명 검색 쿼리
     *
     * Go 서버의 by_retail_store_name.go 로직 변환
     */
    fun buildRetailStoreQuery(
        retailStoreName: String,
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
                        .must { m ->
                            m.nested { n ->
                                n
                                    .path("stock")
                                    .query { sq ->
                                        sq.bool { sb ->
                                            sb
                                                .must { sm ->
                                                    sm.term { t ->
                                                        t
                                                            .field("stock.retail_store_name.lc")
                                                            .value(FieldValue.of(retailStoreName.lowercase()))
                                                    }
                                                }.must { sm ->
                                                    sm.range { r ->
                                                        r
                                                            .field("stock.quantity")
                                                            .gt(JsonData.of(0))
                                                    }
                                                }
                                        }
                                    }
                            }
                        }.filter { f -> f.exists { e -> e.field("selling") } }
                        .mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }
            }.size(size)
            .apply {
                addRetailStoreSorting(cursor)
            }.build()

    /**
     * 키워드 + 필터(셀러타입, 카테고리) 검색 쿼리
     *
     * Go 서버의 by_keyword_with_seller_type_category.go 로직 변환
     */
    fun buildKeywordWithFiltersQuery(
        keyword: String,
        sellerType: String? = null,
        categorySlug: String? = null,
        ordering: String? = null,
        size: Int,
        cursor: List<String>? = null,
    ): SearchRequest {
        val trimmedKeyword = keyword.trim().take(MAX_KEYWORD_LENGTH)

        return SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query { q ->
                q.bool { b ->
                    b.must { m ->
                        m.bool { sb ->
                            sb
                                .should(buildProductNameQuery(trimmedKeyword))
                                .should(buildProductNameNgramQuery(trimmedKeyword))
                                .should(buildDescriptionQuery(trimmedKeyword))
                                .should(buildBarcodeQuery(trimmedKeyword))
                                .should(buildCodeQuery(trimmedKeyword))
                                .should(buildSellerQuery(trimmedKeyword)) // nested seller keyword
                                .should(buildCategoryQuery(trimmedKeyword))
                                .should(buildOptionQuery(trimmedKeyword))
                                .should(buildVariantBarcodeQuery(trimmedKeyword))
                                .minimumShouldMatch("1")
                        }
                    }
                    b.filter { f ->
                        f.bool { fb ->
                            fb.must { it.exists { e -> e.field("display") } }
                            fb.mustNot { it.exists { e -> e.field("deleted") } }
                        }
                    }
                    b.apply {
                        if (!sellerType.isNullOrBlank()) {
                            b.filter { f ->
                                f.nested { n ->
                                    n
                                        .path("seller")
                                        .query { sq ->
                                            sq.term { t ->
                                                t
                                                    .field("seller.type")
                                                    .value(FieldValue.of(sellerType))
                                            }
                                        }
                                }
                            }
                        }

                        // categorySlug 필터
                        if (!categorySlug.isNullOrBlank()) {
                            b.filter { f ->
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
                            }
                        }
                    }
                }
            }.size(size)
            .apply {
                addKeywordFiltersSorting(ordering, trimmedKeyword, cursor)
            }.build()
    }

	/*
	 * 상품 ID 목록으로 검색 (정렬 없이 - 베스트 랭킹, 좋아요 등에서 사용)
	 *
	 * Go 서버의 by_ids.go 로직 변환
	 */
    fun buildProductIdsBulkQuery(
        productIds: List<Long>,
        size: Int,
    ): SearchRequest =
        SearchRequest
            .Builder()
            .index(PRODUCTS_INDEX)
            .query { q ->
                q.bool { b ->
                    b
                        .must { m ->
                            m.terms { t ->
                                t
                                    .field("id")
                                    .terms { tf ->
                                        tf.value(productIds.map { FieldValue.of(it) })
                                    }
                            }
                        }.filter { f -> f.exists { e -> e.field("selling") } }
                        .filter { f -> f.exists { e -> e.field("display") } }
                        .mustNot { mn -> mn.exists { e -> e.field("deleted") } }
                }
            }.size(size)
            .build()

    // ========== Sorting Helper Methods ==========

    /**
     * 기획전 정렬
     */
    private fun SearchRequest.Builder.addDisplayGroupSorting(
        ordering: String?,
        displayGroupId: Long,
        cursor: List<String>?,
    ): SearchRequest.Builder {
        val effectiveOrdering = ordering ?: ORDERING_DISPLAY_GROUP_PRODUCT_SEQ

        effectiveOrdering.split(",").map { it.trim() }.forEach { field ->
            val (sortField, direction) = parseSortField(field)
            val order = if (direction == "desc") SortOrder.Desc else SortOrder.Asc

            when (sortField) {
                ORDERING_IN_STOCK -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("is_selling")
                                .order(SortOrder.Desc)
                                .missing { mv -> mv.longValue(0) }
                        }
                    }
                }

                ORDERING_DISPLAY_GROUP_PRODUCT_SEQ -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("display_group.product_seq")
                                .order(order)
                                .nested { n ->
                                    n
                                        .path("display_group")
                                        .filter { fq ->
                                            fq.term { t ->
                                                t
                                                    .field("display_group.id")
                                                    .value(FieldValue.of(displayGroupId))
                                            }
                                        }
                                }
                        }
                    }
                    sort { s ->
                        s.field { f ->
                            f.field("display").order(SortOrder.Desc)
                        }
                    }
                }
            }
        }

        sort { s -> s.field { f -> f.field("id").order(SortOrder.Asc) } }
        cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }

        return this
    }

    /**
     * 카테고리 + 셀러 정렬
     */
    private fun SearchRequest.Builder.addCategorySellerSorting(
        ordering: String?,
        cursor: List<String>?,
    ): SearchRequest.Builder {
        val effectiveOrdering = ordering ?: "-$ORDERING_RELEASED"

        effectiveOrdering.split(",").map { it.trim() }.forEach { field ->
            val (sortField, direction) = parseSortField(field)
            val order = if (direction == "desc") SortOrder.Desc else SortOrder.Asc

            when (sortField) {
                ORDERING_IN_STOCK -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("is_selling")
                                .order(SortOrder.Desc)
                                .missing { mv -> mv.longValue(0) }
                        }
                    }
                }

                ORDERING_RELEASED -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("released")
                                .order(order)
                                .missing { mv -> mv.longValue(-1) }
                        }
                    }
                }
            }
        }

        sort { s -> s.field { f -> f.field("id").order(SortOrder.Desc) } }
        cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }

        return this
    }

    /**
     * 신상품 정렬
     */
    private fun SearchRequest.Builder.addNewestSorting(
        ordering: String?,
        cursor: List<String>?,
    ): SearchRequest.Builder {
        ordering?.split(",")?.map { it.trim() }?.forEach { field ->
            val (sortField, direction) = parseSortField(field)
            val order = if (direction == "desc") SortOrder.Desc else SortOrder.Asc

            when (sortField) {
                ORDERING_RELEASED -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("released")
                                .order(order)
                                .missing { mv -> mv.longValue(-1) }
                        }
                    }
                }

                ORDERING_PRODUCT_BEST_ORDER -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("best_order.order_count")
                                .order(SortOrder.Desc)
                                .missing { mv -> mv.stringValue("_last") }
                        }
                    }
                    sort { s ->
                        s.field { f ->
                            f
                                .field("best_order.like_count")
                                .order(SortOrder.Desc)
                                .missing { mv -> mv.stringValue("_last") }
                        }
                    }
                    sort { s ->
                        s.field { f ->
                            f
                                .field("best_order.cart_count")
                                .order(SortOrder.Desc)
                                .missing { mv -> mv.stringValue("_last") }
                        }
                    }
                }
            }
        }

        sort { s -> s.field { f -> f.field("id").order(SortOrder.Desc) } }
        cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }

        return this
    }

    /**
     * 카테고리 ID 정렬
     */
    private fun SearchRequest.Builder.addCategoryIdSorting(
        ordering: String?,
        cursor: List<String>?,
    ): SearchRequest.Builder {
        ordering?.split(",")?.map { it.trim() }?.forEach { field ->
            val (sortField, direction) = parseSortField(field)
            val order = if (direction == "desc") SortOrder.Desc else SortOrder.Asc

            when (sortField) {
                ORDERING_RELEASED -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("released")
                                .order(order)
                                .missing { mv -> mv.longValue(-1) }
                        }
                    }
                }

                ORDERING_SALES_AMOUNT -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("best_order.sales_amount")
                                .order(order)
                                .missing { mv -> mv.stringValue("_last") }
                        }
                    }
                }
            }
        }

        sort { s -> s.field { f -> f.field("id").order(SortOrder.Desc) } }
        cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }

        return this
    }

    /**
     * 리테일 스토어 정렬
     */
    private fun SearchRequest.Builder.addRetailStoreSorting(cursor: List<String>?): SearchRequest.Builder {
        sort { s ->
            s.field { f ->
                f
                    .field("best_order.order_count")
                    .order(SortOrder.Desc)
                    .missing { mv -> mv.stringValue("_last") }
            }
        }
        sort { s ->
            s.field { f ->
                f
                    .field("best_order.like_count")
                    .order(SortOrder.Desc)
                    .missing { mv -> mv.stringValue("_last") }
            }
        }
        sort { s -> s.field { f -> f.field("id").order(SortOrder.Desc) } }
        cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }

        return this
    }

    /**
     * 키워드 + 필터 정렬
     */
    private fun SearchRequest.Builder.addKeywordFiltersSorting(
        ordering: String?,
        keyword: String,
        cursor: List<String>?,
    ): SearchRequest.Builder {
        // 키워드가 없고 정렬 조건도 없으면 ID 정렬만
        if (keyword.isBlank() && ordering.isNullOrBlank()) {
            sort { s -> s.field { f -> f.field("id").order(SortOrder.Desc) } }
            cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }
            return this
        }

        // 정렬 조건이 없으면 점수 기반 정렬
        if (ordering.isNullOrBlank()) {
            sort { s -> s.score { it.order(SortOrder.Desc) } }
            sort { s -> s.field { f -> f.field("id").order(SortOrder.Asc) } }
            cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }
            return this
        }

        ordering.split(",").map { it.trim() }.forEach { field ->
            val (sortField, direction) = parseSortField(field)
            val order = if (direction == "desc") SortOrder.Desc else SortOrder.Asc

            when (sortField) {
                ORDERING_RELEASED -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("released")
                                .order(order)
                                .missing { mv -> mv.longValue(-1) }
                        }
                    }
                }

                ORDERING_PRODUCT_BEST_ORDER -> {
                    sort { s ->
                        s.field { f ->
                            f
                                .field("best_order.order_count")
                                .order(SortOrder.Desc)
                                .missing { mv -> mv.stringValue("_last") }
                        }
                    }
                    sort { s ->
                        s.field { f ->
                            f
                                .field("best_order.like_count")
                                .order(SortOrder.Desc)
                                .missing { mv -> mv.stringValue("_last") }
                        }
                    }
                    sort { s ->
                        s.field { f ->
                            f
                                .field("best_order.cart_count")
                                .order(SortOrder.Desc)
                                .missing { mv -> mv.stringValue("_last") }
                        }
                    }
                }
            }
        }

        sort { s -> s.field { f -> f.field("id").order(SortOrder.Desc) } }
        cursor?.let { searchAfter(it.map { v -> FieldValue.of(v) }) }

        return this
    }

    // ========== Utility Methods ==========

    /**
     * 정렬 필드 파싱
     * "-released" -> ("released", "desc")
     * "released" -> ("released", "asc")
     */
    private fun parseSortField(field: String): Pair<String, String> {
        val trimmed = field.trim()
        return if (trimmed.startsWith("-")) {
            trimmed.substring(1) to "desc"
        } else {
            trimmed to "asc"
        }
    }

    /**
     * in_stock 정렬 포함 여부 확인
     */
    private fun containsInStockOrdering(ordering: String?): Boolean {
        if (ordering.isNullOrBlank()) return false

        return ordering.split(",").any { field ->
            val (sortField, _) = parseSortField(field)
            sortField == ORDERING_IN_STOCK
        }
    }
}
