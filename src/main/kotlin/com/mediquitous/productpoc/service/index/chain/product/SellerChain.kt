package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.model.document.AttachmentDocument
import com.mediquitous.productpoc.model.document.BodyFrameType
import com.mediquitous.productpoc.model.document.SellerDocument
import com.mediquitous.productpoc.model.document.SellerType
import com.mediquitous.productpoc.repository.jpa.common.AttachmentJpaRepository
import com.mediquitous.productpoc.repository.jpa.common.StyleTagJpaRepository
import com.mediquitous.productpoc.repository.jpa.seller.SellerJpaRepository
import com.mediquitous.productpoc.service.index.chain.BaseIndexChain
import com.mediquitous.productpoc.service.index.chain.IndexContext
import com.mediquitous.productpoc.service.index.chain.ProductDocumentBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 셀러 정보 체인
 *
 * Go 서버의 seller_chain.go 로직을 Kotlin으로 변환
 */
@Component
class SellerChain(
    private val sellerRepository: SellerJpaRepository,
    private val attachmentRepository: AttachmentJpaRepository,
    private val styleTagRepository: StyleTagJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id
        val sellerId = builder.sellerId

        if (sellerId == null) {
            logger.error { "셀러 ID가 없음: productId=$productId" }
            return null
        }

        logger.debug { "SellerChain 처리 시작: productId=$productId, sellerId=$sellerId" }

        // 1. 셀러 조회
        val seller = sellerRepository.findNullableById(sellerId)
        if (seller == null) {
            logger.error { "셀러를 찾을 수 없음: sellerId=$sellerId" }
            return null
        }

        // 2. 프로필 이미지 조회
        val profileImage =
            seller.profileImageId?.let { imageId ->
                attachmentRepository.findByIds(listOf(imageId)).firstOrNull()?.let {
                    AttachmentDocument(
                        id = it.id!!,
                        mimeType = it.mimetype,
                        file = it.file,
                        seq = it.seq,
                    )
                }
            }

        // 3. 스타일 태그 조회
        val styleTags =
            try {
                styleTagRepository.findBySellerId(sellerId).map { it.name ?: "" }
            } catch (e: Exception) {
                logger.warn { "스타일 태그 조회 실패: sellerId=$sellerId" }
                emptyList()
            }

        // 4. 추가 태그 생성 (디렉터/트렌드 쇼핑몰만)
        val extraTags =
            if (seller.type == SellerType.DIRECTOR || seller.type == SellerType.TREND_SHOPPINGMALL) {
                buildList {
                    seller.influencerName?.let { add(it) }
                    seller.height?.let { add("${it}cm") }
                    seller.bodyFrameType?.let { type ->
                        BodyFrameType.toJapanese(type)?.let { add(it) }
                    }
                }
            } else {
                emptyList()
            }

        // 5. 키워드 배열 생성
        val keywordArray =
            seller.keywords
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

        // 6. 셀러 문서 생성
        builder.seller =
            SellerDocument(
                id = seller.id!!,
                partnerId = seller.partnerId,
                name = seller.name ?: "",
                code = seller.code,
                type = seller.type ?: "",
                targetGender = seller.targetGender ?: "",
                slug = seller.slug,
                segment = seller.segment,
                brandName = seller.brandName ?: "",
                brandNameJp = seller.brandNameJp,
                status = seller.status ?: "",
                isOfficialBrand = seller.isOfficialBrand ?: false,
                profileImage = profileImage,
                instagram = seller.instagram,
                tiktok = seller.tiktok,
                influencerName = seller.influencerName ?: "",
                influencerNameJp = seller.influencerNameJp ?: "",
                height = seller.height,
                weight = seller.weight,
                bodyFrameType = seller.bodyFrameType,
                topSize = seller.topSize,
                bottomSize = seller.bottomSize,
                shoeSize = seller.shoeSize,
                styleTags = styleTags,
                extraTags = extraTags,
                keywords = seller.keywords,
                keywordArray = keywordArray,
            )

        logger.debug { "SellerChain 처리 완료: productId=$productId, sellerId=$sellerId" }
        return next(context)
    }
}
