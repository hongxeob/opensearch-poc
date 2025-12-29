package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.model.document.AttachmentDocument
import com.mediquitous.productpoc.model.document.GuideImageDocument
import com.mediquitous.productpoc.repository.jpa.product.ProductGuideImageJpaRepository
import com.mediquitous.productpoc.service.index.chain.BaseIndexChain
import com.mediquitous.productpoc.service.index.chain.IndexContext
import com.mediquitous.productpoc.service.index.chain.ProductDocumentBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 가이드 이미지 체인
 *
 * Go 서버의 guide_image_chain.go 로직을 Kotlin으로 변환
 */
@Component
class GuideImageChain(
    private val guideImageRepository: ProductGuideImageJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id
        val guideImageId = builder.guideImageId ?: return next(context)

        logger.debug { "GuideImageChain 처리 시작: productId=$productId, guideImageId=$guideImageId" }

        return runBlocking {
            // DB 조회를 비동기로 시작 (Go의 go func에 해당)
            val guideImageDeferred =
                async(Dispatchers.IO) {
                    guideImageRepository.findWithAttachmentById(guideImageId)
                }

            // 다음 체인 실행 (병렬)
            val nextResult = next(context)

            // DB 조회 결과 대기 (Go의 wg.Wait()에 해당)
            val guideImage = guideImageDeferred.await()

            if (guideImage != null) {
                builder.guideImage =
                    GuideImageDocument(
                        id = guideImage.id,
                        name = guideImage.name,
                        image =
                            guideImage.attachmentId?.let { attachmentId ->
                                AttachmentDocument(
                                    id = attachmentId,
                                    mimeType = guideImage.mimetype,
                                    file = guideImage.file,
                                    seq = guideImage.seq,
                                )
                            },
                    )
            }

            logger.debug { "GuideImageChain 처리 완료: productId=$productId" }
            nextResult
        }
    }
}
