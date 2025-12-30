package com.mediquitous.productpoc.service.product

import com.mediquitous.productpoc.model.document.OptionDocument
import com.mediquitous.productpoc.model.document.ProductDocument
import com.mediquitous.productpoc.model.dto.AttachmentDto
import com.mediquitous.productpoc.model.dto.BenefitDto
import com.mediquitous.productpoc.model.dto.CategoryDto
import com.mediquitous.productpoc.model.dto.CouponDto
import com.mediquitous.productpoc.model.dto.GuideImageDto
import com.mediquitous.productpoc.model.dto.OptionGroup
import com.mediquitous.productpoc.model.dto.OptionValue
import com.mediquitous.productpoc.model.dto.ProductDto
import com.mediquitous.productpoc.model.dto.ProductOption
import com.mediquitous.productpoc.model.dto.ProductVariant
import com.mediquitous.productpoc.model.dto.SellerDto
import com.mediquitous.productpoc.model.dto.SimpleProductDto
import com.mediquitous.productpoc.model.dto.Stock
import com.mediquitous.productpoc.repository.jpa.benefit.BenefitExcludeDisplayGroupJpaRepository
import com.mediquitous.productpoc.repository.jpa.benefit.BenefitJpaRepository
import com.mediquitous.productpoc.repository.jpa.benefit.CouponSettingJpaRepository
import com.mediquitous.productpoc.repository.jpa.benefit.entity.BenefitEntity
import com.mediquitous.productpoc.repository.jpa.benefit.entity.CouponSettingEntity
import com.mediquitous.productpoc.repository.jpa.displaygroup.DisplayGroupJpaRepository
import com.mediquitous.productpoc.service.index.chain.ProductDocumentBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val logger = KotlinLogging.logger {}

private const val CDN_URL = "https://cdn.nugu.jp/public/" // TODO: 설정으로 분리

/**
 * 상품 변환 서비스 구현체
 *
 * Go 서버의 internal/service/product/convert_service.go 변환
 */
@Service
class ProductConvertServiceImpl(
    private val displayGroupJpaRepository: DisplayGroupJpaRepository,
    private val benefitJpaRepository: BenefitJpaRepository,
    private val benefitExcludeDisplayGroupJpaRepository: BenefitExcludeDisplayGroupJpaRepository,
    private val couponSettingJpaRepository: CouponSettingJpaRepository,
) : ProductConvertService {
    override fun convertToProductDto(builders: List<ProductDocumentBuilder>): List<ProductDto> = convert(builders)

    override fun convertToSimpleProductDto(builders: List<ProductDocumentBuilder>): List<SimpleProductDto> =
        convert(builders).map {
            it.toSimple()
        }

    private fun convert(builders: List<ProductDocumentBuilder>): List<ProductDto> {
        if (builders.isEmpty()) return emptyList()

        val products = builders.map { builderToProduct(it) }
        applyDisplayGroup(products)
        applyBenefit(products)
        applyCoupon(products)

        return products
    }

    private fun builderToProduct(builder: ProductDocumentBuilder): ProductDto {
        val seller = builder.seller
        val bestOrder = builder.bestOrder

        return ProductDto(
            id = builder.id,
            code = builder.code,
            name = builder.name,
            englishName = builder.englishName,
            slug = builder.slug,
            internalName = builder.internalName,
            customCode = builder.customCode,
            price = builder.price,
            display = builder.display?.atOffset(ZoneOffset.UTC),
            selling = builder.selling?.atOffset(ZoneOffset.UTC),
            title = builder.title,
            annotation = builder.annotation,
            quantityLimitType = builder.quantityLimitType,
            quantityLimit = builder.quantityLimit,
            weight = builder.weight,
            material = builder.material,
            clothFabric = builder.clothFabric,
            deleted = builder.deleted?.atOffset(ZoneOffset.UTC),
            released = builder.released?.atOffset(ZoneOffset.UTC),
            season = builder.season,
            sizeInfo = builder.sizeInfo,
            optionType = builder.optionType,
            modelName = builder.modelName,
            memberOnly = builder.memberOnly,
            repurchasable = builder.repurchasable,
            info = builder.info,
            origin = builder.originId,
            trend = builder.trendId,
            express = builder.express,
            iconSet = builder.labels.toMutableList(),
            isOriginal = builder.isOriginal,
            hasShoeCategory = builder.hasShoeCategory,
            relatedProducts = builder.relatedProductIds.toList(),
            seller = seller?.name ?: "",
            sellerSlug = seller?.slug,
            sellerStatus = seller?.status ?: "",
            sellerDisplay = seller?.display ?: false,
            brand = seller?.brandName,
            sellerDtoObject =
                seller?.let {
                    SellerDto(
                        id = it.id,
                        name = it.name,
                        type = it.type,
                        brandName = it.brandName,
                        influencerName = it.influencerName,
                        slug = it.slug,
                        segment = it.segment,
                        code = it.code,
                        profileImage =
                            it.profileImage?.let { img ->
                                AttachmentDto(id = img.id, file = CDN_URL + (img.file ?: ""))
                            },
                        instagram = it.instagram,
                        tiktok = it.tiktok,
                        status = it.status,
                        isOfficialBrand = it.isOfficialBrand,
                        styleTags = it.styleTagsJp,
                        extraTags = it.extraTags,
                        totalLikeCount = it.totalLikeCount,
                        openAt = it.openAt?.atOffset(ZoneOffset.UTC),
                        display = it.display,
                        newProductBegin = it.newProductBegin?.atOffset(ZoneOffset.UTC),
                        newProductEnd = it.newProductEnd?.atOffset(ZoneOffset.UTC),
                        targetGender = it.targetGender,
                        keywords = it.keywords,
                        keywordArray = it.keywordArray,
                    )
                },
            reviewCount = bestOrder?.reviewCount ?: 0,
            reviewAverage = bestOrder?.reviewAverage ?: 0.0,
            totalLikeCount = bestOrder?.totalLikeCount ?: 0,
            image = builder.image?.let { AttachmentDto(id = it.id, file = CDN_URL + (it.file ?: "")) },
            imageSet = builder.images.map { CDN_URL + it },
            guideImageDto =
                builder.guideImage?.let {
                    GuideImageDto(
                        id = it.id,
                        file = it.image?.file?.let { f -> CDN_URL + f },
                    )
                },
            leafCategories =
                builder.categories.filter { it.isLeaf }.map {
                    CategoryDto(
                        it.id,
                        it.parentId,
                        it.name ?: "",
                        it.displayName ?: "",
                        it.slug,
                        it.isVisible,
                        it.isLeaf,
                    )
                },
            optionSet =
                builder.options.map {
                    ProductOption(
                        id = it.id,
                        name = it.name ?: "",
                        value = it.value ?: "",
                        hexcode = it.hexcode,
                        searchName = it.searchName,
                        model = it.model ?: false,
                    )
                },
            options = buildOptionGroups(builder.options),
            productVariantSet =
                builder.variants.map { v ->
                    ProductVariant(
                        id = v.id,
                        code = v.code,
                        barcode = v.barcode,
                        externalBarcode = v.externalBarcode,
                        barcode2 = v.barcode2,
                        display = v.display?.atOffset(ZoneOffset.UTC),
                        selling = v.selling?.atOffset(ZoneOffset.UTC),
                        deleted = v.deleted?.atOffset(ZoneOffset.UTC),
                        additionalPrice = v.additionalPrice,
                        useInventory = v.useInventory,
                        displaySoldout = v.displaySoldout,
                        inventoryType = v.inventoryType,
                        quantityCheckType = v.quantityCheckType,
                        quantity = v.quantity,
                        safetyQuantity = v.safetyQuantity,
                        price = builder.price + v.additionalPrice,
                        discountPrice = builder.price + v.additionalPrice,
                    )
                },
            stock = builder.stock.map { Stock(it.id, it.productVariantId, it.quantity) },
            displayGroupIds = builder.displayGroup.map { it.id }.toMutableSet(),
        )
    }

    private fun buildOptionGroups(options: List<OptionDocument>): List<OptionGroup> =
        options.groupBy { it.name }.map { (name, opts) ->
            OptionGroup(
                name = name ?: "",
                values = opts.map { OptionValue(it.id, it.valueSeq, it.value ?: "", it.hexcode) }.sortedBy { it.seq },
            )
        }

    private fun applyDisplayGroup(productDtos: List<ProductDto>) {
        val ids = productDtos.flatMap { it.displayGroupIds }.distinct()
        if (ids.isEmpty()) return
        val active = displayGroupJpaRepository.findActiveDisplayGroupIdsByIds(ids).toSet()
        productDtos.forEach { it.filterDisplayGroupIds(active) }
    }

    private fun applyBenefit(productDtos: List<ProductDto>) {
        val fixed = getFixedDiscountBenefits(productDtos)
        val rate = getRateDiscountBenefits(productDtos)
        productDtos.forEach { it.applyBenefits(fixed, rate) }
    }

    private fun getFixedDiscountBenefits(productDtos: List<ProductDto>): Map<Long, List<BenefitDto>> {
        val productIds = productDtos.map { it.id }
        if (productIds.isEmpty()) return emptyMap()
        // TODO: BenefitProduct 테이블 JOIN으로 productId 기준 그룹핑 필요
        val entities = benefitJpaRepository.findActiveBenefitsByProductIds(productIds)
        return entities.mapNotNull { entityToBenefit(it) }.groupBy { it.id }
    }

    private fun getRateDiscountBenefits(productDtos: List<ProductDto>): Map<Long, List<BenefitDto>> {
        val displayGroupIds = productDtos.flatMap { it.displayGroupIds }.distinct()
        if (displayGroupIds.isEmpty()) return emptyMap()

        val entities = benefitJpaRepository.findActiveBenefitsByDisplayGroupIds(displayGroupIds)
        val benefitIds = entities.map { it.id!! }

        val excludeMap =
            if (benefitIds.isNotEmpty()) {
                benefitExcludeDisplayGroupJpaRepository
                    .findByBenefitIds(benefitIds)
                    .groupBy({ it.benefitId!! }, { it.displayGroupId!! })
            } else {
                emptyMap()
            }

        return entities
            .filter { it.displayGroupId != null }
            .mapNotNull { entity ->
                entityToBenefit(entity, excludeMap[entity.id] ?: emptyList())?.let { entity.displayGroupId!! to it }
            }.groupBy({ it.first }, { it.second })
    }

    private fun applyCoupon(productDtos: List<ProductDto>) {
        val couponSettings = getCouponSettings(productDtos)
        val now = OffsetDateTime.now()
        productDtos.forEach { it.applyCouponSettings(couponSettings, now) }
    }

    private fun getCouponSettings(productDtos: List<ProductDto>): Map<Long, List<CouponDto>> {
        val displayGroupIds = productDtos.flatMap { it.displayGroupIds }.distinct()
        val entities = couponSettingJpaRepository.findActiveCouponSettingsByDisplayGroupIds(displayGroupIds)

        return entities
            .mapNotNull { entityToCoupon(it)?.let { c -> (it.displayGroupId ?: 0L) to c } }
            .groupBy({ it.first }, { it.second })
    }

    private fun entityToBenefit(
        entity: BenefitEntity,
        excludeDisplayGroupIds: List<Long> = emptyList(),
    ): BenefitDto? {
        if (entity.begin == null || entity.end == null) return null
        val detailSetting =
            try {
                @Suppress("UNCHECKED_CAST")
                entity.detailSetting as? Map<String, Any>
            } catch (e: Exception) {
                null
            }

        val discountValue =
            detailSetting?.get("discount_value")?.let {
                when (it) {
                    is Number -> it.toDouble()
                    is String -> it.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
            } ?: 0.0

        return BenefitDto(
            id = entity.id!!,
            seq = entity.seq,
            isPinned = entity.isPinned,
            name = entity.name ?: "",
            internalName = entity.internalName,
            activated = entity.activated ?: false,
            deleted = entity.deleted,
            type = entity.type ?: "",
            begin = entity.begin!!,
            end = entity.end!!,
            minimumOrderAmount = entity.minimumOrderAmount?.toDouble(),
            detailSetting = detailSetting,
            discountValue = discountValue,
            isFixedDiscountByProduct = entity.isFixedDiscountByProduct ?: false,
            isCollaboration = entity.isCollaboration ?: false,
            excludeDisplayGroupIds = excludeDisplayGroupIds,
        )
    }

    private fun entityToCoupon(entity: CouponSettingEntity): CouponDto? {
        if (entity.issueBegin == null || entity.issueEnd == null) return null
        return CouponDto(
            id = entity.id!!,
            code = entity.code ?: "",
            name = entity.name ?: "",
            description = entity.description,
            group = entity.group,
            benefitType = entity.benefitType ?: "",
            benefitValue = entity.benefitValue?.toDouble() ?: 0.0,
            benefitMaxValue = entity.benefitMaxValue?.toDouble(),
            issueType = entity.issueType ?: "",
            issueMaxCount = entity.issueMaxCount,
            issueMaxCountByUser = entity.issueMaxCountByUser ?: 0,
            availableMinPrice = entity.availableMinPrice?.toDouble() ?: 0.0,
            availableCouponCountByOrder = entity.availableCouponCountByOrder ?: 0,
            availablePeriodType = entity.availablePeriodType ?: "",
            availableBegin = entity.availableBegin,
            availableEnd = entity.availableEnd,
            availableDayFromIssued = entity.availableDayFromIssued,
            showProductDetail = entity.showProductDetail ?: false,
            deleted = entity.deleted,
            type = entity.type ?: "",
            benefitPercentageRoundUnit = entity.benefitPercentageRoundUnit,
            amountType = entity.amountType,
            issuedCount = entity.issuedCount ?: 0,
            includeRegionalShippingRate = entity.includeRegionalShippingRate ?: false,
            availablePlatform = entity.availablePlatform ?: "",
            issueBegin = entity.issueBegin!!,
            issueEnd = entity.issueEnd!!,
            isPaused = entity.isPaused ?: false,
            targetType = entity.targetType ?: "",
            autoIssueType = entity.autoIssueType,
            availableFixedDateBegin = entity.availableFixedDateBegin,
            availableFixedDateEnd = entity.availableFixedDateEnd,
            availableFixedTimeBegin = entity.availableFixedTimeBegin,
            availableFixedTimeEnd = entity.availableFixedTimeEnd,
            displaygroup = entity.displayGroupId,
            excludeDisplaygroup = entity.excludeDisplayGroupId,
        )
    }

    override fun convertDocumentsToSimpleProducts(documents: List<ProductDocument>): List<SimpleProductDto> {
        if (documents.isEmpty()) return emptyList()

        // ProductDocument -> ProductDocumentBuilder로 변환
        val builders = documents.map { documentToBuilder(it) }

        // 기존 변환 로직 활용
        return convertToSimpleProductDto(builders)
    }

    /**
     * ProductDocument를 ProductDocumentBuilder로 변환
     */
    private fun documentToBuilder(doc: ProductDocument): ProductDocumentBuilder =
        ProductDocumentBuilder(id = doc.id).apply {
            code = doc.code
            name = doc.name
            englishName = doc.englishName
            slug = doc.slug
            internalName = doc.internalName
            customCode = doc.customCode
            price = doc.price
            display = doc.display
            selling = doc.selling
            title = doc.title
            annotation = doc.annotation
            quantityLimitType = doc.quantityLimitType
            quantityLimit = doc.quantityLimit
            weight = doc.weight
            material = doc.material
            clothFabric = doc.clothFabric
            deleted = doc.deleted
            released = doc.released
            season = doc.season
            sizeInfo = doc.sizeInfo
            optionType = doc.optionType
            modelName = doc.modelName
            memberOnly = doc.memberOnly
            repurchasable = doc.repurchasable
            info = doc.info
            originId = doc.originId
            trendId = doc.trendId
            express = doc.express
            labels = doc.label.toMutableList()
            isOriginal = doc.isOriginal
            hasShoeCategory = doc.hasShoeCategory
            relatedProductIds = doc.relatedProductIds.toMutableList()
            image = doc.image
            images = doc.images.toMutableList()
            guideImage = doc.guideImage
            seller = doc.seller
            bestOrder = doc.bestOrder
            options = doc.options.toMutableList()
            variants = doc.variants.toMutableList()
            stock = doc.stock.toMutableList()
            categories = doc.categories.toMutableList()
            displayGroup = doc.displayGroup.toMutableList()
        }
}
