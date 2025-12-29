package com.mediquitous.productpoc.service.index.chain

import com.mediquitous.productpoc.model.document.ProductDocument
import com.mediquitous.productpoc.service.index.chain.product.BestOrderChain
import com.mediquitous.productpoc.service.index.chain.product.CategoryChain
import com.mediquitous.productpoc.service.index.chain.product.DisplayGroupChain
import com.mediquitous.productpoc.service.index.chain.product.GuideImageChain
import com.mediquitous.productpoc.service.index.chain.product.ImageChain
import com.mediquitous.productpoc.service.index.chain.product.ImagesChain
import com.mediquitous.productpoc.service.index.chain.product.LabelChain
import com.mediquitous.productpoc.service.index.chain.product.OptionsChain
import com.mediquitous.productpoc.service.index.chain.product.ProductChain
import com.mediquitous.productpoc.service.index.chain.product.RelatedProductChain
import com.mediquitous.productpoc.service.index.chain.product.SellerChain
import com.mediquitous.productpoc.service.index.chain.product.StockChain
import com.mediquitous.productpoc.service.index.chain.product.VariantsChain
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 상품 인덱싱 체인 조립기
 *
 * Go 서버의 NewIndexService 함수에서 체인 조립 로직을 분리
 * Chain of Responsibility 패턴으로 상품 문서를 단계별로 조립
 *
 * 체인 순서:
 * Product → Seller → Variants → Category → DisplayGroup
 * → Image → Images → Label → GuideImage → BestOrder
 * → RelatedProduct → Options → Stock
 */
@Component
class ProductIndexChainAssembler(
    private val productChain: ProductChain,
    private val sellerChain: SellerChain,
    private val variantsChain: VariantsChain,
    private val categoryChain: CategoryChain,
    private val displayGroupChain: DisplayGroupChain,
    private val imageChain: ImageChain,
    private val imagesChain: ImagesChain,
    private val labelChain: LabelChain,
    private val guideImageChain: GuideImageChain,
    private val bestOrderChain: BestOrderChain,
    private val relatedProductChain: RelatedProductChain,
    private val optionsChain: OptionsChain,
    private val stockChain: StockChain,
) {
    /**
     * 체인을 조립하고 루트 체인 반환
     */
    fun assembleChain(): IndexChain<ProductDocumentBuilder> {
        // 체인 연결 (Go 서버의 SetNext 호출 순서와 동일)
        productChain
            .setNext(sellerChain)
            .setNext(variantsChain)
            .setNext(categoryChain)
            .setNext(displayGroupChain)
            .setNext(imageChain)
            .setNext(imagesChain)
            .setNext(labelChain)
            .setNext(guideImageChain)
            .setNext(bestOrderChain)
            .setNext(relatedProductChain)
            .setNext(optionsChain)
            .setNext(stockChain)

        return productChain
    }

    /**
     * 상품 ID로 ProductDocument 생성
     *
     * @param productId 상품 ID
     * @return 조립된 ProductDocument (삭제 대상이면 null)
     */
    fun buildProductDocument(productId: Long): ProductDocument? {
        logger.debug { "상품 문서 조립 시작: productId=$productId" }

        val chain = assembleChain()
        val builder = ProductDocumentBuilder(productId)
        val context = IndexContext(builder)

        val result = chain.handle(context)

        return result?.build()?.also {
            logger.debug { "상품 문서 조립 완료: productId=$productId" }
        } ?: run {
            logger.debug { "상품 문서 조립 실패 (삭제 대상): productId=$productId" }
            null
        }
    }
}
