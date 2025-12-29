package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.model.document.DisplayGroupDocument
import com.mediquitous.productpoc.repository.jpa.displaygroup.DisplayGroupProductJpaRepository
import com.mediquitous.productpoc.service.index.chain.BaseIndexChain
import com.mediquitous.productpoc.service.index.chain.IndexContext
import com.mediquitous.productpoc.service.index.chain.ProductDocumentBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 기획전(Display Group) 정보 체인
 *
 * Go 서버의 display_group_chain.go 로직을 Kotlin으로 변환
 */
@Component
class DisplayGroupChain(
    private val displayGroupProductRepository: DisplayGroupProductJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        logger.debug { "DisplayGroupChain 처리 시작: productId=$productId" }

        // 기획전 상품 조회
        val displayGroups =
            displayGroupProductRepository
                .findByProductId(productId)
                .map { dgp ->
                    DisplayGroupDocument(
                        id = dgp.groupId ?: 0L,
                        productSeq = dgp.seq ?: 0,
                    )
                }.sortedBy { it.productSeq }

        builder.displayGroup.addAll(displayGroups)

        logger.debug { "DisplayGroupChain 처리 완료: productId=$productId, displayGroupCount=${displayGroups.size}" }
        return next(context)
    }
}
