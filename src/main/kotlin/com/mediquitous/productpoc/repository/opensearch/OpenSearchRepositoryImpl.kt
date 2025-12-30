@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.mediquitous.productpoc.repository.opensearch

import com.mediquitous.productpoc.model.document.ProductDocument
import com.mediquitous.productpoc.repository.opensearch.OpenSearchRepository.SearchResult
import com.mediquitous.productpoc.repository.opensearch.query.ProductSearchQueryBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.opensearch.client.opensearch.OpenSearchClient
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
    ): SearchResult {
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
    ): SearchResult {
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
    ): SearchResult {
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

    override fun searchByProductIds(
        productIds: List<Long>,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult {
        logger.debug { "OpenSearch 상품 ID 목록 검색: productIds=${productIds.size}개, size=$size" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildProductIdsQuery(
                productIds = productIds,
                size = size + 1, // hasNext 판단용
                ordering = ordering,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "상품 ID 목록 검색 실패: productIds=$productIds" }
                throw OpenSearchException("상품 ID 목록 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchByDisplayGroupId(
        displayGroupId: Long,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult {
        logger.debug { "OpenSearch 기획전 검색: displayGroupId=$displayGroupId" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildDisplayGroupQuery(
                displayGroupId = displayGroupId,
                size = size + 1,
                ordering = ordering,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "기획전 검색 실패: displayGroupId=$displayGroupId" }
                throw OpenSearchException("기획전 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchByCategoryAndSellerSlug(
        categorySlug: String,
        sellerSlug: String,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult {
        logger.debug { "OpenSearch 카테고리+셀러 검색: categorySlug=$categorySlug, sellerSlug=$sellerSlug" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildCategoryAndSellerSlugQuery(
                categorySlug = categorySlug,
                sellerSlug = sellerSlug,
                size = size + 1,
                ordering = ordering,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "카테고리+셀러 검색 실패: categorySlug=$categorySlug, sellerSlug=$sellerSlug" }
                throw OpenSearchException("카테고리+셀러 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchByHomeTab(
        tabType: String,
        size: Int,
        cursor: String?,
    ): SearchResult {
        logger.debug { "OpenSearch 홈탭 검색: tabType=$tabType" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildHomeTabQuery(
                tabType = tabType,
                size = size + 1,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "홈탭 검색 실패: tabType=$tabType" }
                throw OpenSearchException("홈탭 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchNewest(
        sellerType: String?,
        releasedGte: String?,
        categorySlug: String?,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): SearchResult {
        logger.debug { "OpenSearch 신상품 검색: sellerType=$sellerType, categorySlug=$categorySlug" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildNewestQuery(
                sellerType = sellerType,
                releasedGte = releasedGte,
                categorySlug = categorySlug,
                ordering = ordering,
                size = size + 1,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "신상품 검색 실패" }
                throw OpenSearchException("신상품 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchByRecommendCodes(
        codes: List<String>,
        size: Int,
        cursor: String?,
    ): SearchResult {
        logger.debug { "OpenSearch 추천 상품 검색: codes=${codes.size}개" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildRecommendByCodesQuery(
                codes = codes,
                size = size + 1,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "추천 상품 검색 실패: codes=$codes" }
                throw OpenSearchException("추천 상품 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchByCategoryId(
        categoryId: Long,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult {
        logger.debug { "OpenSearch 카테고리 ID 검색: categoryId=$categoryId" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildCategoryIdQuery(
                categoryId = categoryId,
                size = size + 1,
                ordering = ordering,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "카테고리 ID 검색 실패: categoryId=$categoryId" }
                throw OpenSearchException("카테고리 ID 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchByRetailStoreName(
        retailStoreName: String,
        size: Int,
        ordering: String?,
        cursor: String?,
    ): SearchResult {
        logger.debug { "OpenSearch 리테일스토어 검색: retailStoreName=$retailStoreName" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildRetailStoreQuery(
                retailStoreName = retailStoreName,
                size = size + 1,
                ordering = ordering,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "리테일스토어 검색 실패: retailStoreName=$retailStoreName" }
                throw OpenSearchException("리테일스토어 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchByKeywordWithFilters(
        keyword: String,
        sellerType: String?,
        categorySlug: String?,
        ordering: String?,
        size: Int,
        cursor: String?,
    ): SearchResult {
        logger.debug { "OpenSearch 키워드+필터 검색: keyword=$keyword, sellerType=$sellerType, categorySlug=$categorySlug" }

        val cursorValues = cursor?.let { decodeCursor(it) }
        val searchRequest =
            ProductSearchQueryBuilder.buildKeywordWithFiltersQuery(
                keyword = keyword,
                sellerType = sellerType,
                categorySlug = categorySlug,
                ordering = ordering,
                size = size + 1,
                cursor = cursorValues,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "키워드+필터 검색 실패: keyword=$keyword" }
                throw OpenSearchException("키워드+필터 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, size)
    }

    override fun searchByProductIdsBulk(productIds: List<Long>): SearchResult {
        logger.debug { "OpenSearch 상품 ID 벌크 검색: productIds=${productIds.size}개" }

        val searchRequest =
            ProductSearchQueryBuilder.buildProductIdsBulkQuery(
                productIds = productIds,
                size = productIds.size,
            )

        val response =
            try {
                openSearchClient.search(searchRequest, Map::class.java)
            } catch (e: Exception) {
                logger.error(e) { "상품 ID 벌크 검색 실패: productIds=$productIds" }
                throw OpenSearchException("상품 ID 벌크 검색 중 오류가 발생했습니다", e)
            }

        return parseSearchResponse(response, productIds.size)
    }

    // ========== Private Helper Functions ==========

    /**
     * OpenSearch 응답을 SearchResult로 변환
     */
    private fun parseSearchResponse(
        response: SearchResponse<Map<*, *>>,
        requestedSize: Int,
    ): SearchResult {
        val hits = response.hits().hits()
        val totalHits = response.hits().total()?.value() ?: 0L

        // Map -> ProductDocument 변환
        val documents =
            hits.mapNotNull { hit ->
                val source = hit.source() ?: return@mapNotNull null
                convertToProductDocument(source)
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

        logger.debug { "검색 완료: totalHits=$totalHits, resultSize=${documents.size}" }

        return SearchResult(
            totalHits = totalHits,
            documents = documents,
            nextCursor = nextCursor,
        )
    }

    /**
     * OpenSearch 문서(Map)를 ProductDocument로 변환
     */
    @Suppress("UNCHECKED_CAST")
    private fun convertToProductDocument(source: Map<*, *>): ProductDocument {
        val seller = source["seller"] as? Map<*, *>
        val bestOrder = source["best_order"] as? Map<*, *>
        val image = source["image"] as? Map<*, *>
        val guideImage = source["guide_image"] as? Map<*, *>

        return ProductDocument(
            id = (source["id"] as? Number)?.toLong() ?: 0L,
            code = source["code"] as? String,
            customCode = source["custom_code"] as? String,
            slug = source["slug"] as? String,
            name = source["name"] as? String,
            englishName = source["english_name"] as? String,
            internalName = source["internal_name"] as? String,
            modelName = source["model_name"] as? String,
            description = source["description"] as? String,
            title = source["title"] as? String,
            label = (source["label"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            express = source["express"] as? Boolean ?: false,
            annotation = source["annotation"] as? String,
            brandId = (source["brand_id"] as? Number)?.toLong(),
            trendId = (source["trend_id"] as? Number)?.toLong(),
            image = image?.let { parseAttachmentDocument(it) },
            images = (source["images"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            guideImage = guideImage?.let { parseGuideImageDocument(it) },
            info = source["info"],
            sizeInfo = source["size_info"] as? String,
            price = (source["price"] as? Number)?.toDouble() ?: 0.0,
            material = source["material"] as? String,
            clothFabric = source["cloth_fabric"] as? String,
            weight = (source["weight"] as? Number)?.toDouble(),
            season = source["season"] as? String,
            originId = (source["origin_id"] as? Number)?.toLong(),
            manufacturerId = (source["manufacturer_id"] as? Number)?.toLong(),
            optionType = source["option_type"] as? String ?: "",
            memberOnly = source["member_only"] as? Boolean ?: false,
            quantityLimitType = source["quantity_limit_type"] as? String ?: "",
            quantityLimit = (source["quantity_limit"] as? Number)?.toInt(),
            repurchasable = source["repurchasable"] as? Boolean ?: false,
            display = parseInstant(source["display"]),
            selling = parseInstant(source["selling"]),
            isSelling = source["is_selling"] as? Boolean ?: false,
            released = parseInstant(source["released"]),
            deleted = parseInstant(source["deleted"]),
            bestOrder = bestOrder?.let { parseBestOrderDocument(it) },
            seller = seller?.let { parseSellerDocument(it) },
            options = parseOptionDocuments(source["options"] as? List<*>),
            variants = parseVariantDocuments(source["variants"] as? List<*>),
            stock = parseStockDocuments(source["stock"] as? List<*>),
            isOriginal = source["is_original"] as? Boolean ?: false,
            hasShoeCategory = source["has_shoe_category"] as? Boolean ?: false,
            categories = parseCategoryDocuments(source["categories"] as? List<*>),
            displayGroup = parseDisplayGroupDocuments(source["display_group"] as? List<*>),
            relatedProductIds = (source["related_product_ids"] as? List<*>)?.mapNotNull { (it as? Number)?.toLong() } ?: emptyList(),
        )
    }

    private fun parseInstant(value: Any?): java.time.Instant? =
        when (value) {
            is String -> try { java.time.Instant.parse(value) } catch (e: Exception) { null }
            is Number -> java.time.Instant.ofEpochMilli(value.toLong())
            else -> null
        }

    private fun parseAttachmentDocument(map: Map<*, *>): com.mediquitous.productpoc.model.document.AttachmentDocument =
        com.mediquitous.productpoc.model.document.AttachmentDocument(
            id = (map["id"] as? Number)?.toLong() ?: 0L,
            mimeType = map["mime_type"] as? String,
            file = map["file"] as? String,
            seq = (map["seq"] as? Number)?.toInt(),
        )

    private fun parseGuideImageDocument(map: Map<*, *>): com.mediquitous.productpoc.model.document.GuideImageDocument {
        val imageMap = map["image"] as? Map<*, *>
        return com.mediquitous.productpoc.model.document.GuideImageDocument(
            id = (map["id"] as? Number)?.toLong() ?: 0L,
            name = map["name"] as? String,
            image = imageMap?.let { parseAttachmentDocument(it) },
        )
    }

    private fun parseBestOrderDocument(map: Map<*, *>): com.mediquitous.productpoc.model.document.BestOrderDocument =
        com.mediquitous.productpoc.model.document.BestOrderDocument(
            orderCount = (map["order_count"] as? Number)?.toInt() ?: 0,
            likeCount = (map["like_count"] as? Number)?.toInt() ?: 0,
            cartCount = (map["cart_count"] as? Number)?.toInt() ?: 0,
            viewCount = (map["view_count"] as? Number)?.toInt() ?: 0,
            reviewAverage = (map["review_average"] as? Number)?.toDouble(),
            reviewCount = (map["review_count"] as? Number)?.toInt() ?: 0,
            totalLikeCount = (map["total_like_count"] as? Number)?.toInt() ?: 0,
            salesAmount = (map["sales_amount"] as? Number)?.toInt() ?: 0,
            discountedPrice = (map["discounted_price"] as? Number)?.toDouble() ?: 0.0,
        )

    @Suppress("UNCHECKED_CAST")
    private fun parseSellerDocument(map: Map<*, *>): com.mediquitous.productpoc.model.document.SellerDocument {
        val profileImage = map["profile_image"] as? Map<*, *>
        return com.mediquitous.productpoc.model.document.SellerDocument(
            id = (map["id"] as? Number)?.toLong() ?: 0L,
            name = (map["name"] as? String) ?: "",
            type = (map["type"] as? String) ?: "",
            brandName = (map["brand_name"] as? String) ?: "",
            influencerName = (map["influencer_name"] as? String) ?: "",
            slug = map["slug"] as? String,
            segment = map["segment"] as? String,
            code = map["code"] as? String,
            profileImage = profileImage?.let { parseAttachmentDocument(it) },
            instagram = map["instagram"] as? String,
            tiktok = map["tiktok"] as? String,
            status = (map["status"] as? String) ?: "",
            isOfficialBrand = map["is_official_brand"] as? Boolean ?: false,
            styleTagsJp = (map["style_tags_jp"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            extraTags = (map["extra_tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            totalLikeCount = (map["total_like_count"] as? Number)?.toLong(),
            openAt = parseInstant(map["open_at"]),
            display = map["display"] as? Boolean ?: false,
            newProductBegin = parseInstant(map["new_product_begin"]),
            newProductEnd = parseInstant(map["new_product_end"]),
            targetGender = (map["target_gender"] as? String) ?: "",
            keywords = map["keywords"] as? String,
            keywordArray = (map["keyword_array"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseOptionDocuments(list: List<*>?): List<com.mediquitous.productpoc.model.document.OptionDocument> =
        list?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            com.mediquitous.productpoc.model.document.OptionDocument(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                name = map["name"] as? String,
                value = map["value"] as? String,
                hexcode = map["hexcode"] as? String,
                searchName = map["search_name"] as? String,
                model = map["model"] as? Boolean,
                nameSeq = (map["name_seq"] as? Number)?.toInt(),
                valueSeq = (map["value_seq"] as? Number)?.toInt(),
            )
        } ?: emptyList()

    @Suppress("UNCHECKED_CAST")
    private fun parseVariantDocuments(list: List<*>?): List<com.mediquitous.productpoc.model.document.VariantDocument> =
        list?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            com.mediquitous.productpoc.model.document.VariantDocument(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                code = map["code"] as? String,
                useInventory = map["use_inventory"] as? Boolean ?: false,
                displaySoldout = map["display_soldout"] as? Boolean ?: false,
                inventoryType = map["inventory_type"] as? String,
                quantityCheckType = map["quantity_check_type"] as? String,
                quantity = (map["quantity"] as? Number)?.toInt() ?: 0,
                safetyQuantity = (map["safety_quantity"] as? Number)?.toInt() ?: 0,
                barcode = map["barcode"] as? String,
                barcode2 = map["barcode2"] as? String,
                externalBarcode = map["external_barcode"] as? String,
                deleted = parseInstant(map["deleted"]),
                optionIds = (map["option_ids"] as? List<*>)?.mapNotNull { (it as? Number)?.toLong() } ?: emptyList(),
                options = map["options"] as? Map<String, Any>,
                additionalPrice = (map["additional_price"] as? Number)?.toDouble() ?: 0.0,
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                display = parseInstant(map["display"]),
                selling = parseInstant(map["selling"]),
                soldOut = map["sold_out"] as? Boolean ?: false,
                express = map["express"] as? Boolean ?: false,
                availableStockQuantities = (map["available_stock_quantities"] as? Number)?.toInt() ?: 0,
            )
        } ?: emptyList()

    private fun parseStockDocuments(list: List<*>?): List<com.mediquitous.productpoc.model.document.StockDocument> =
        list?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            com.mediquitous.productpoc.model.document.StockDocument(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                productVariantId = (map["product_variant_id"] as? Number)?.toLong() ?: 0L,
                quantity = (map["quantity"] as? Number)?.toInt() ?: 0,
                warehouseId = (map["warehouse_id"] as? Number)?.toLong(),
                warehouseName = map["warehouse_name"] as? String,
                retailStoreName = map["retail_store_name"] as? String,
                isQuickDelivery = map["is_quick_delivery"] as? Boolean ?: false,
            )
        } ?: emptyList()

    private fun parseCategoryDocuments(list: List<*>?): List<com.mediquitous.productpoc.model.document.CategoryDocument> =
        list?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            com.mediquitous.productpoc.model.document.CategoryDocument(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                parentId = (map["parent_id"] as? Number)?.toLong(),
                name = map["name"] as? String,
                displayName = map["display_name"] as? String,
                slug = map["slug"] as? String,
                isVisible = map["is_visible"] as? Boolean ?: false,
                isLeaf = map["is_leaf"] as? Boolean ?: false,
            )
        } ?: emptyList()

    private fun parseDisplayGroupDocuments(list: List<*>?): List<com.mediquitous.productpoc.model.document.DisplayGroupDocument> =
        list?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            com.mediquitous.productpoc.model.document.DisplayGroupDocument(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
            )
        } ?: emptyList()

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
