package com.mediquitous.productpoc.service.index.chain

import com.mediquitous.productpoc.model.document.AttachmentDocument
import com.mediquitous.productpoc.model.document.BestOrderDocument
import com.mediquitous.productpoc.model.document.CategoryDocument
import com.mediquitous.productpoc.model.document.DisplayGroupDocument
import com.mediquitous.productpoc.model.document.GuideImageDocument
import com.mediquitous.productpoc.model.document.OptionDocument
import com.mediquitous.productpoc.model.document.ProductDocument
import com.mediquitous.productpoc.model.document.SellerDocument
import com.mediquitous.productpoc.model.document.StockDocument
import com.mediquitous.productpoc.model.document.VariantDocument
import java.time.Instant

/**
 * ProductDocument 빌더
 *
 * Chain of Responsibility 패턴에서 각 체인이 문서를 점진적으로 조립
 * Go 서버의 dao.Product 포인터 수정 방식을 Kotlin 빌더 패턴으로 변환
 */
class ProductDocumentBuilder(
    val id: Long,
) {
    // 기본 정보
    var code: String? = null
    var customCode: String? = null
    var slug: String? = null
    var name: String? = null
    var englishName: String? = null
    var internalName: String? = null
    var modelName: String? = null
    var description: String? = null
    var title: String? = null
    var labels: MutableList<String> = mutableListOf()
    var express: Boolean = false
    var annotation: String? = null
    var brandId: Long? = null
    var trendId: Long? = null

    // 이미지
    var image: AttachmentDocument? = null
    var imageId: Long? = null // 이미지 조회용 임시 저장
    var images: MutableList<String> = mutableListOf()
    var guideImage: GuideImageDocument? = null
    var guideImageId: Long? = null // 가이드 이미지 조회용 임시 저장

    // 상세 정보
    var info: Any? = null
    var sizeInfo: String? = null
    var price: Double = 0.0
    var material: String? = null
    var clothFabric: String? = null
    var weight: Double? = null
    var season: String? = null
    var originId: Long? = null
    var manufacturerId: Long? = null
    var optionType: String = ""

    // 판매 설정
    var memberOnly: Boolean = false
    var quantityLimitType: String = ""
    var quantityLimit: Int? = null
    var repurchasable: Boolean = false

    // 날짜
    var display: Instant? = null
    var selling: Instant? = null
    var isSelling: Boolean = false
    var released: Instant? = null
    var deleted: Instant? = null

    // 연관 데이터
    var bestOrder: BestOrderDocument? = null
    var seller: SellerDocument? = null
    var sellerId: Long? = null // 셀러 조회용 임시 저장
    var options: MutableList<OptionDocument> = mutableListOf()
    var variants: MutableList<VariantDocument> = mutableListOf()
    var stock: MutableList<StockDocument> = mutableListOf()

    // 카테고리
    var isOriginal: Boolean = false
    var hasShoeCategory: Boolean = false
    var categories: MutableList<CategoryDocument> = mutableListOf()

    // 기획전, 연관 상품
    var displayGroup: MutableList<DisplayGroupDocument> = mutableListOf()
    var relatedProductIds: MutableList<Long> = mutableListOf()

    /**
     * ProductDocument 빌드
     */
    fun build(): ProductDocument =
        ProductDocument(
            id = id,
            code = code,
            customCode = customCode,
            slug = slug,
            name = name,
            englishName = englishName,
            internalName = internalName,
            modelName = modelName,
            description = description,
            title = title,
            label = labels.toList(),
            express = express,
            annotation = annotation,
            brandId = brandId,
            trendId = trendId,
            image = image,
            images = images.toList(),
            guideImage = guideImage,
            info = info,
            sizeInfo = sizeInfo,
            price = price,
            material = material,
            clothFabric = clothFabric,
            weight = weight,
            season = season,
            originId = originId,
            manufacturerId = manufacturerId,
            optionType = optionType,
            memberOnly = memberOnly,
            quantityLimitType = quantityLimitType,
            quantityLimit = quantityLimit,
            repurchasable = repurchasable,
            display = display,
            selling = selling,
            isSelling = isSelling,
            released = released,
            deleted = deleted,
            bestOrder = bestOrder,
            seller = seller,
            options = options.toList(),
            variants = variants.toList(),
            stock = stock.toList(),
            isOriginal = isOriginal,
            hasShoeCategory = hasShoeCategory,
            categories = categories.toList(),
            displayGroup = displayGroup.toList(),
            relatedProductIds = relatedProductIds.toList(),
        )
}
