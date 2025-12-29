package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.model.document.OptionDocument
import com.mediquitous.productpoc.repository.jpa.product.OptionJpaRepository
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
 * 옵션 체인
 *
 * Go 서버의 options_chain.go 로직을 Kotlin으로 변환
 */
@Component
class OptionsChain(
    private val optionRepository: OptionJpaRepository,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        logger.debug { "OptionsChain 처리 시작: productId=$productId" }

        return runBlocking {
            // DB 조회를 비동기로 시작
            val optionsDeferred =
                async(Dispatchers.IO) {
                    optionRepository.findByProductId(productId)
                }

            // 다음 체인 실행 (병렬)
            val nextResult = next(context)

            // DB 조회 결과 대기
            val options = optionsDeferred.await()

            options.forEach { option ->
                builder.options.add(
                    OptionDocument(
                        id = option.id!!,
                        name = option.name,
                        value = option.value,
                        hexcode = option.hexcode,
                        searchName = option.searchName,
                        model = option.model,
                        nameSeq = option.nameSeq,
                        valueSeq = option.valueSeq,
                    ),
                )
            }

            logger.debug { "OptionsChain 처리 완료: productId=$productId, optionCount=${builder.options.size}" }
            nextResult
        }
    }
}
