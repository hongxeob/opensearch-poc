package com.mediquitous.productpoc.service.search

import com.mediquitous.productpoc.model.document.ProductDocument
import com.mediquitous.productpoc.model.dto.CursorPaginationResponse
import com.mediquitous.productpoc.model.dto.SimpleProductDto
import com.mediquitous.productpoc.model.vo.BestRankingPath
import com.mediquitous.productpoc.model.vo.LikedProduct
import com.mediquitous.productpoc.model.vo.RankedProduct
import com.mediquitous.productpoc.repository.jpa.customer.CustomerEventJpaRepository
import com.mediquitous.productpoc.repository.jpa.customer.LikeJpaRepository
import com.mediquitous.productpoc.repository.jpa.ranking.ProductRankingJpaRepository
import com.mediquitous.productpoc.repository.jpa.ranking.RankingSpecificationJpaRepository
import com.mediquitous.productpoc.repository.opensearch.OpenSearchRepository
import com.mediquitous.productpoc.repository.opensearch.OpenSearchRepository.SearchResult
import com.mediquitous.productpoc.service.product.ProductConvertService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger {}

// 랭킹 기간 상수 (Go: Days, Weekly, Monthly)
private const val RANKING_PERIOD_DAILY = 1
private const val RANKING_PERIOD_WEEKLY = 7
private const val RANKING_PERIOD_MONTHLY = 30

/**
 * 상품 검색 서비스 구현체
 *
 * Go 서버의 product service 계층을 마이그레이션
 */
@Service
class ProductSearchServiceImpl(
    private val openSearchRepository: OpenSearchRepository,
    private val productConvertService: ProductConvertService,
    private val rankingSpecificationJpaRepository: RankingSpecificationJpaRepository,
    private val productRankingJpaRepository: ProductRankingJpaRepository,
    private val likeJpaRepository: LikeJpaRepository,
    private val customerEventJpaRepository: CustomerEventJpaRepository,
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
            results = productConvertService.convertDocumentsToSimpleProducts(searchResult.documents),
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
        // todo -> 대체 X 고쳐야함
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
        rankingInfo: BestRankingPath,
        size: Int,
        cursor: String?,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "베스트 랭킹 검색: path = ${rankingInfo.path}, period=${rankingInfo.period}, size=$size, cursor=$cursor" }

        // 2. 랭킹 스펙 ID 조회
        val specificationId = rankingSpecificationJpaRepository.findIdByPath(rankingInfo.path)
        if (specificationId == null) {
            logger.warn { "랭킹 스펙을 찾을 수 없습니다: path=${rankingInfo.path}" }
            return CursorPaginationResponse.empty()
        }

        // 3. 랭킹 상품 ID 목록 조회 (pageSize + 1로 다음 페이지 여부 확인)
        val rankedProducts = getRankedProductIds(specificationId, size, cursor)
        if (rankedProducts.isEmpty()) {
            logger.debug { "랭킹 상품이 없습니다: specificationId=$specificationId" }
            return CursorPaginationResponse.empty()
        }

        // 4. 다음 커서 계산 (size + 1개를 조회했으므로)
        val hasNext = rankedProducts.size > size
        val nextCursor =
            if (hasNext) {
                val nextRank = rankedProducts[size].rank
                encodeIntCursor(nextRank)
            } else {
                null
            }

        // 5. 실제 반환할 상품 ID 목록 (size 개수만큼)
        val productIdsToSearch = rankedProducts.take(size).map { it.productId }

        // 6. OpenSearch에서 상품 정보 조회
        val searchResult = openSearchRepository.searchByProductIdsBulk(productIdsToSearch)

        // 7. 원본 랭킹 순서 복원
        val orderedProducts = restoreOriginalRankingOrder(productIdsToSearch, searchResult.documents)

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = orderedProducts,
            nextCursor = nextCursor,
            previousCursor = null,
        )
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
        logger.info { "좋아요 상품 검색: customerId=$customerId, size=$size, cursor=$cursor" }

        // 1. 파라미터 검증
        require(customerId > 0) { "customerId는 양수여야 합니다" }

        // 2. 좋아요 상품 ID 목록 조회 (size + 1로 다음 페이지 여부 확인)
        val likedProducts = getLikedProductIds(customerId, size, cursor)
        if (likedProducts.isEmpty()) {
            logger.debug { "좋아요한 상품이 없습니다: customerId=$customerId" }
            return CursorPaginationResponse.empty()
        }

        // 3. 다음 커서 계산 (size + 1개를 조회했으므로)
        val hasNext = likedProducts.size > size
        val nextCursor =
            if (hasNext) {
                val nextLikeId = likedProducts[size].likeId
                encodeLongCursor(nextLikeId)
            } else {
                null
            }

        // 4. 실제 반환할 상품 ID 목록 (size 개수만큼)
        val productIdsToSearch = likedProducts.take(size).map { it.productId }

        // 5. OpenSearch에서 상품 정보 조회
        val searchResult = openSearchRepository.searchByProductIdsBulk(productIdsToSearch)

        // 6. 원본 좋아요 순서 복원 (최신 좋아요 순)
        val orderedProducts = restoreOriginalRankingOrder(productIdsToSearch, searchResult.documents)

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = orderedProducts,
            nextCursor = nextCursor,
            previousCursor = null,
        )
    }

    override fun getRecentlyViewedProducts(
        customerId: Long,
        size: Int,
    ): CursorPaginationResponse<SimpleProductDto> {
        logger.info { "최근 본 상품 검색: customerId=$customerId, size=$size" }

        // 1. 최근 본 상품 ID 목록 조회
        val recentlyViewedProductIds =
            customerEventJpaRepository
                .findRecentlyViewedProductIdsByCustomerId(customerId, size)
                .map { it.productId }

        if (recentlyViewedProductIds.isEmpty()) {
            logger.debug { "최근 본 상품이 없습니다: customerId=$customerId" }
            return CursorPaginationResponse.empty()
        }

        // 2. OpenSearch에서 상품 정보 조회
        val searchResult = openSearchRepository.searchByProductIdsBulk(recentlyViewedProductIds)

        // 3. 원본 조회 순서 복원 (최근 본 순)
        val orderedProducts = restoreOriginalRankingOrder(recentlyViewedProductIds, searchResult.documents)

        return CursorPaginationResponse(
            count = orderedProducts.size.toLong(),
            results = orderedProducts,
            nextCursor = null,
            previousCursor = null,
        )
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
        searchResult: SearchResult,
        size: Int,
    ): CursorPaginationResponse<SimpleProductDto> {
        val hasNext = searchResult.documents.size > size
        val documents = if (hasNext) searchResult.documents.take(size) else searchResult.documents
        val nextCursor = if (hasNext) searchResult.nextCursor else null

        // ProductDocument -> SimpleProduct 변환
        val results = productConvertService.convertDocumentsToSimpleProducts(documents)

        logger.debug { "검색 완료: totalHits=${searchResult.totalHits}, resultSize=${results.size}, hasNext=$hasNext" }

        return CursorPaginationResponse(
            count = searchResult.totalHits,
            results = results,
            nextCursor = nextCursor,
            previousCursor = null,
        )
    }

    // =====================================================
    // Best Ranking Helper Methods
    // =====================================================

    private fun isValidRankingPeriod(period: Int): Boolean =
        period == RANKING_PERIOD_DAILY || period == RANKING_PERIOD_WEEKLY || period == RANKING_PERIOD_MONTHLY

    /**
     * 랭킹 상품 ID 조회 (커서 유무에 따라 분기)
     */
    private fun getRankedProductIds(
        specificationId: Long,
        size: Int,
        cursor: String?,
    ): List<RankedProduct> {
        val limit = size + 1 // 다음 페이지 여부 확인용
        val decodedCursor = cursor?.let { decodeIntCursor(it) } ?: 0

        val projections =
            if (decodedCursor == 0) {
                productRankingJpaRepository.findRankedProductIds(specificationId, limit)
            } else {
                productRankingJpaRepository.findRankedProductIdsByCursor(specificationId, decodedCursor, limit)
            }

        return projections.map { projection ->
            RankedProduct(
                productId = projection.productId,
                rank = projection.rank,
            )
        }
    }

    /**
     * 원본 랭킹 순서 복원
     *
     * OpenSearch 검색 결과를 원본 상품 ID 순서로 재정렬
     */
    private fun restoreOriginalRankingOrder(
        originalIds: List<Long>,
        documents: List<ProductDocument>,
    ): List<SimpleProductDto> {
        if (documents.isEmpty()) return emptyList()

        val orderMap = originalIds.withIndex().associate { (index, id) -> id to index }

        val sortedDocuments =
            documents.sortedBy { document ->
                orderMap[document.id] ?: Int.MAX_VALUE
            }

        return productConvertService.convertDocumentsToSimpleProducts(sortedDocuments)
    }

    /**
     * Int 커서 인코딩 (Base64)
     */
    private fun encodeIntCursor(value: Int): String =
        Base64
            .getUrlEncoder()
            .encodeToString(value.toString().toByteArray())

    /**
     * Int 커서 디코딩 (Base64)
     */
    private fun decodeIntCursor(cursor: String): Int =
        try {
            String(
                Base64
                    .getUrlDecoder()
                    .decode(cursor),
            ).toInt()
        } catch (e: Exception) {
            logger.warn { "커서 디코딩 실패: $cursor" }
            0
        }

    // =====================================================
    // Liked Products Helper Methods
    // =====================================================

    /**
     * 좋아요 상품 ID 조회 (커서 유무에 따라 분기)
     */
    private fun getLikedProductIds(
        customerId: Long,
        size: Int,
        cursor: String?,
    ): List<LikedProduct> {
        val limit = size + 1 // 다음 페이지 여부 확인용
        val pageable = PageRequest.of(0, limit)
        val decodedCursor = cursor?.let { decodeLongCursor(it) }

        val projections =
            if (decodedCursor == null) {
                likeJpaRepository.findLikedProductIdsByCustomerId(customerId, pageable)
            } else {
                likeJpaRepository.findLikedProductIdsByCustomerIdWithCursor(customerId, decodedCursor, pageable)
            }

        return projections.map { projection ->
            LikedProduct(
                likeId = projection.likeId,
                productId = projection.productId,
            )
        }
    }

    /**
     * Long 커서 인코딩 (Base64)
     */
    private fun encodeLongCursor(value: Long): String =
        Base64
            .getUrlEncoder()
            .encodeToString(value.toString().toByteArray())

    /**
     * Long 커서 디코딩 (Base64)
     */
    private fun decodeLongCursor(cursor: String): Long? =
        try {
            String(
                Base64
                    .getUrlDecoder()
                    .decode(cursor),
            ).toLong()
        } catch (e: Exception) {
            logger.warn { "Long 커서 디코딩 실패: $cursor" }
            null
        }
}
