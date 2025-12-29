package com.mediquitous.productpoc.service.index.chain.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.model.document.AttachmentDocument
import com.mediquitous.productpoc.model.document.GuideImageDocument
import com.mediquitous.productpoc.repository.jpa.product.ProductJpaRepository
import com.mediquitous.productpoc.service.index.chain.BaseIndexChain
import com.mediquitous.productpoc.service.index.chain.IndexContext
import com.mediquitous.productpoc.service.index.chain.ProductDocumentBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * CS 처리용 배송비 결제 상품 코드 (검색/목록 노출 제외)
 */
private val IGNORE_PRODUCT_CODES = setOf("P000EJGJ", "DERBJU5043", "DKEBSZ5325")

/**
 * 상품 기본 정보 체인
 *
 * Go 서버의 product_chain.go 로직을 Kotlin으로 변환
 * PostgreSQL에서 상품 기본 정보를 조회하여 문서에 설정
 */
@Component
class ProductChain(
    private val productRepository: ProductJpaRepository,
    private val objectMapper: ObjectMapper,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        logger.debug { "ProductChain 처리 시작: productId=$productId" }

        // 1. 상품 조회
        val product = productRepository.findNullableById(productId)
        if (product == null) {
            logger.warn { "상품을 찾을 수 없음: productId=$productId" }
            return null // 삭제 대상
        }

        // 2. 코드 검증
        if (product.code == null) {
            logger.warn { "상품 코드가 없음: productId=$productId" }
            return null
        }

        // 3. 제외 상품 체크
        if (product.code in IGNORE_PRODUCT_CODES) {
            logger.debug { "제외 대상 상품: productId=$productId, code=${product.code}" }
            return null
        }

        // 4. 삭제된 상품 체크
        if (product.deleted != null) {
            logger.debug { "삭제된 상품: productId=$productId" }
            return null
        }

        // 5. 셀러 ID 검증
        if (product.sellerId == null) {
            logger.warn { "셀러 ID가 없음: productId=$productId" }
            return null
        }

        // 6. 기본 정보 설정
        builder.apply {
            code = product.code
            customCode = product.customCode
            slug = product.slug
            name = product.name
            englishName = product.englishName
            internalName = product.internalName
            modelName = product.modelName
            title = product.title
            annotation = product.annotation
            brandId = product.brandId
            trendId = product.trendId

            // 이미지 ID 임시 저장 (ImageChain에서 조회)
            imageId = product.imageId
            if (product.imageId != null) {
                image = AttachmentDocument(id = product.imageId!!)
            }

            // 가이드 이미지 ID 임시 저장
            guideImageId = product.guideImageId
            if (product.guideImageId != null) {
                guideImage = GuideImageDocument(id = product.guideImageId!!)
            }

            // Description HTML 제거
            description = product.description?.let { stripHtml(it) }

            // Info JSON 파싱
            info = product.info?.let { parseJson(it) }

            sizeInfo = product.sizeInfo

            // 가격 (필수)
            price = product.price?.toDouble() ?: run {
                logger.error { "가격이 없음: productId=$productId" }
                return null
            }

            material = product.material
            clothFabric = product.clothFabric
            weight = product.weight?.toDouble()
            season = product.season
            originId = product.originId
            manufacturerId = product.manufacturerId
            optionType = product.optionType ?: ""

            memberOnly = product.memberOnly ?: false
            quantityLimitType = product.quantityLimitType ?: ""
            quantityLimit = product.quantityLimit
            repurchasable = product.repurchasable ?: false

            display = product.display?.toInstant()
            selling = product.selling?.toInstant()
            isSelling = product.selling != null
            released = product.released?.toInstant()
            deleted = product.deleted?.toInstant()

            // 셀러 ID 저장 (SellerChain에서 조회)
            sellerId = product.sellerId
        }

        logger.debug { "ProductChain 처리 완료: productId=$productId" }
        return next(context)
    }

    /**
     * HTML 태그 제거
     */
    private fun stripHtml(html: String): String =
        html
            .replace(Regex("<[^>]*>"), "")
            .replace(Regex("&[a-zA-Z]+;"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    /**
     * JSON 문자열 파싱
     */
    private fun parseJson(json: String): Any? =
        try {
            objectMapper.readValue(json, Any::class.java)
        } catch (e: Exception) {
            logger.warn { "JSON 파싱 실패: $json" }
            null
        }
}
