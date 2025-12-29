package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.model.document.AttachmentDocument
import com.mediquitous.productpoc.repository.jpa.common.AttachmentJpaRepository
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
 * 대표 이미지 체인
 *
 * Go 서버의 image_chain.go 로직을 Kotlin으로 변환
 */
@Component
class ImageChain(
    private val attachmentRepository: AttachmentJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id
        val imageId = builder.imageId ?: return next(context)

        logger.debug { "ImageChain 처리 시작: productId=$productId, imageId=$imageId" }

        return runBlocking {
            // DB 조회를 비동기로 시작
            val attachmentsDeferred =
                async(Dispatchers.IO) {
                    attachmentRepository.findByIds(listOf(imageId))
                }

            // 다음 체인 실행 (병렬)
            val nextResult = next(context)

            // DB 조회 결과 대기
            val attachments = attachmentsDeferred.await()

            if (attachments.isNotEmpty()) {
                val attachment = attachments.first()
                builder.image =
                    AttachmentDocument(
                        id = attachment.id!!,
                        mimeType = attachment.mimetype,
                        file = attachment.file,
                        seq = attachment.seq,
                    )
            }

            logger.debug { "ImageChain 처리 완료: productId=$productId" }
            nextResult
        }
    }
}

/**
 * 상품 이미지 목록 체인
 *
 * Go 서버의 images_chain.go 로직을 Kotlin으로 변환
 */
@Component
class ImagesChain(
    private val attachmentRepository: AttachmentJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        logger.debug { "ImagesChain 처리 시작: productId=$productId" }
        return runBlocking {
            // DB 조회를 비동기로 시작
            val attachmentsDeferred =
                async(Dispatchers.IO) {
                    attachmentRepository.findByProductId(productId)
                }

            // 다음 체인 실행 (병렬)
            val nextResult = next(context)

            // DB 조회 결과 대기
            val attachments = attachmentsDeferred.await()

            // Go: attachment.File만 추출
            attachments.forEach { attachment ->
                attachment.file?.let { builder.images.add(it) }
            }

            logger.debug { "ImagesChain 처리 완료: productId=$productId, imageCount=${builder.images.size}" }
            nextResult
        }
    }
}
