package com.mediquitous.productpoc.service.index.chain.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.model.document.VariantDocument
import com.mediquitous.productpoc.repository.jpa.product.ProductVariantJpaRepository
import com.mediquitous.productpoc.repository.jpa.product.ProductVariantOptionSetJpaRepository
import com.mediquitous.productpoc.service.index.chain.BaseIndexChain
import com.mediquitous.productpoc.service.index.chain.IndexContext
import com.mediquitous.productpoc.service.index.chain.ProductDocumentBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 품목(Variant) 정보 체인
 *
 * Go 서버의 variants_chain.go 로직을 Kotlin으로 변환
 */
@Component
class VariantsChain(
    private val variantRepository: ProductVariantJpaRepository,
    private val variantOptionSetRepository: ProductVariantOptionSetJpaRepository,
    private val objectMapper: ObjectMapper,
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        logger.debug { "VariantsChain 처리 시작: productId=$productId" }

        // 1. 품목 조회
        val variants = variantRepository.findByProductId(productId)
        if (variants.isEmpty()) {
            logger.debug { "품목이 없음: productId=$productId" }
            return next(context)
        }

        // 2. 품목 옵션 조회
        val variantIds = variants.mapNotNull { it.id }
        val optionIdsByVariantId =
            if (variantIds.isNotEmpty()) {
                variantOptionSetRepository
                    .findByProductVariantIds(variantIds)
                    .groupBy { it.productVariantId }
                    .mapValues { (_, options) -> options.mapNotNull { it.optionId } }
            } else {
                emptyMap()
            }

        // 3. 품목 문서 생성
        variants.forEach { variant ->
            // 삭제된 품목 제외 (Go: if deleted != nil { continue })
            if (variant.deleted != null) return@forEach

            // 옵션 ID가 없으면 제외 (Go: if !ok { continue })
            val optionIds = optionIdsByVariantId[variant.id]
            if (optionIds.isNullOrEmpty()) {
                logger.warn { "품목 옵션이 없음: variantId=${variant.id}" }
                return@forEach
            }

            val options =
                variant.options?.let {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        objectMapper.readValue(it, Map::class.java) as? Map<String, Any>
                    } catch (e: Exception) {
                        logger.error { "품목 옵션 JSON 파싱 실패: variantId=${variant.id}" }
                        null
                    }
                }

            val additionalPrice = variant.additionalPrice?.toDouble() ?: 0.0
            val price = builder.price + additionalPrice

            builder.variants.add(
                VariantDocument(
                    id = variant.id!!,
                    code = variant.code,
                    useInventory = variant.useInventory ?: false,
                    displaySoldout = variant.displaySoldout ?: false,
                    inventoryType = variant.inventoryType,
                    quantityCheckType = variant.quantityCheckType,
                    quantity = variant.quantity ?: 0,
                    safetyQuantity = variant.safetyQuantity ?: 0,
                    barcode = variant.barcode,
                    barcode2 = variant.barcode2,
                    externalBarcode = variant.externalBarcode,
                    deleted = variant.deleted?.toInstant(),
                    optionIds = optionIds,
                    options = options,
                    additionalPrice = additionalPrice,
                    price = price,
                    display = variant.display?.toInstant(),
                    selling = variant.selling?.toInstant(),
                    // Go: variant.UseInventory && variant.Quantity <= 0 && variant.SafetyQuantity <= 0
                    soldOut =
                        (variant.useInventory == true) &&
                            (variant.quantity ?: 0) <= 0 &&
                            (variant.safetyQuantity ?: 0) <= 0,
                ),
            )
        }

        logger.debug { "VariantsChain 처리 완료: productId=$productId, variantCount=${builder.variants.size}" }
        return next(context)
    }
}
