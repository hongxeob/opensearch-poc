package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.repository.jpa.common.IconJpaRepository
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
 * 라벨(아이콘) 체인
 *
 * Go 서버의 label_chain.go 로직을 Kotlin으로 변환
 */
@Component
class LabelChain(
    private val iconRepository: IconJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        logger.debug { "LabelChain 처리 시작: productId=$productId" }
        return runBlocking {
            // 아이콘(라벨) 조회
            val iconsDeferred =
                async(Dispatchers.IO) {
                    iconRepository.findByProductId(productId)
                }
            // 다음 체인 실행 (병렬)
            val nextResult = next(context)

            // DB 조회 결과 대기
            val icons = iconsDeferred.await()

            // Go: icon.Name이 nil이 아닌 것만 추가
            icons.forEach { icon ->
                icon.name?.let { builder.labels.add(it) }
            }

            logger.debug { "LabelChain 처리 완료: productId=$productId, labelCount=${builder.labels.size}" }
            nextResult
        }
    }
}
