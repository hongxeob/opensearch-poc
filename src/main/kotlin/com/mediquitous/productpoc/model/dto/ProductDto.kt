package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import kotlin.math.floor

/**
 * 상품 전체 정보 DTO
 *
 * Go 서버의 internal/service/dto/product.go 변환
 *
 * 비즈니스 로직 포함:
 * - 할인가 계산 (정액/정률)
 * - 혜택 적용
 * - 쿠폰 적용
 * - 아이콘 설정
 */
data class ProductDto(
    val id: Long,
    var seller: String = "",
    @JsonProperty("seller_slug")
    var sellerSlug: String? = null,
    @JsonProperty("seller_status")
    var sellerStatus: String = "",
    @JsonProperty("seller_display")
    var sellerDisplay: Boolean = false,
    var brand: String? = null,
    var image: AttachmentDto? = null,
    @JsonProperty("leaf_categories")
    var leafCategories: List<CategoryDto> = emptyList(),
    @JsonProperty("discount_price")
    var discountPrice: Double = 0.0,
    @JsonProperty("review_count")
    var reviewCount: Int = 0,
    @JsonProperty("review_average")
    var reviewAverage: Double = 0.0,
    @JsonProperty("total_like_count")
    var totalLikeCount: Int = 0,
    var express: Boolean = false,
    @JsonProperty("icon_set")
    var iconSet: MutableList<String> = mutableListOf(),
    @JsonProperty("is_original")
    var isOriginal: Boolean = false,
    @JsonProperty("guide_image")
    var guideImageDto: GuideImageDto? = null,
    @JsonProperty("image_set")
    var imageSet: List<String> = emptyList(),
    @JsonProperty("discount_rate")
    var discountRate: Double = 0.0,
    @JsonProperty("seller_object")
    var sellerDtoObject: SellerDto? = null,
    @JsonProperty("shipping_fee_benefit")
    var shippingFeeBenefitDto: BenefitDto? = null,
    @JsonProperty("has_shoe_category")
    var hasShoeCategory: Boolean = false,
    @JsonProperty("productvariant_set")
    var productVariantSet: List<ProductVariant> = emptyList(),
    var stock: List<Stock> = emptyList(),
    @JsonProperty("option_set")
    var optionSet: List<ProductOption> = emptyList(),
    var options: List<OptionGroup> = emptyList(),
    @JsonProperty("benefit_end")
    var benefitEnd: OffsetDateTime? = null,
    @JsonProperty("coupon_settings")
    var couponDtoSettings: List<CouponDto> = emptyList(),
    var code: String? = null,
    var name: String? = null,
    @JsonProperty("english_name")
    var englishName: String? = null,
    var slug: String? = null,
    @JsonProperty("internal_name")
    var internalName: String? = null,
    @JsonProperty("custom_code")
    var customCode: String? = null,
    var price: Double = 0.0,
    var display: OffsetDateTime? = null,
    var selling: OffsetDateTime? = null,
    var title: String? = null,
    var annotation: String? = null,
    var description: String? = null,
    @JsonProperty("quantity_limit_type")
    var quantityLimitType: String = "",
    @JsonProperty("quantity_limit")
    var quantityLimit: Int? = null,
    var weight: Double? = null,
    var material: String? = null,
    @JsonProperty("cloth_fabric")
    var clothFabric: String? = null,
    var deleted: OffsetDateTime? = null,
    var released: OffsetDateTime? = null,
    var season: String? = null,
    @JsonProperty("size_info")
    var sizeInfo: String? = null,
    @JsonProperty("option_type")
    var optionType: String = "",
    @JsonProperty("model_name")
    var modelName: String? = null,
    @JsonProperty("member_only")
    var memberOnly: Boolean = false,
    var repurchasable: Boolean = false,
    var info: Any? = null,
    var origin: Long? = null,
    var trend: Long? = null,
    @JsonProperty("related_products")
    var relatedProducts: List<Long> = emptyList(),
    @JsonIgnore
    var displayGroupIds: MutableSet<Long> = mutableSetOf(),
) {
    companion object {
        const val COUPON_ICON_NAME = "쿠폰"
    }

    // =====================================================
    // 비즈니스 로직 메서드
    // =====================================================

    /**
     * 정액 할인가 설정
     */
    fun applyFixedDiscountPrice(newDiscountPrice: Double) {
        discountRate =
            if (price > 0) {
                floor((price - newDiscountPrice) / price * 100)
            } else {
                0.0
            }
        discountPrice = newDiscountPrice

        // Variant에도 동일한 할인가 적용
        productVariantSet =
            productVariantSet.map { variant ->
                variant.copy(discountPrice = newDiscountPrice)
            }
    }

    /**
     * 정률 할인율 설정
     */
    fun applyDiscountRate(newDiscountRate: Double) {
        discountPrice = floor(price * (100 - newDiscountRate) / 100)
        discountRate = newDiscountRate

        // Variant에도 동일한 할인율 적용
        productVariantSet =
            productVariantSet.map { variant ->
                val variantPrice = price + variant.additionalPrice
                variant.copy(discountPrice = floor(variantPrice * (100 - newDiscountRate) / 100))
            }
    }

    /**
     * 아이콘 추가 (중복 방지)
     */
    fun addIcon(icon: String) {
        if (icon !in iconSet) {
            iconSet.add(icon)
        }
    }

    /**
     * 활성화된 기획전 ID만 필터링
     */
    fun filterDisplayGroupIds(activeDisplayGroupIds: Set<Long>) {
        displayGroupIds = displayGroupIds.filter { it in activeDisplayGroupIds }.toMutableSet()
    }

    /**
     * 특정 기획전에 속해있는지 확인
     */
    fun hasDisplayGroup(vararg ids: Long): Boolean = ids.any { it in displayGroupIds }

    /**
     * 혜택 적용
     *
     * @param fixedDiscountBenefits 상품별 정액 할인 혜택 (productId -> benefits)
     * @param rateDiscountBenefits 기획전별 정률 할인 혜택 (displayGroupId -> benefits)
     */
    fun applyBenefits(
        fixedDiscountBenefits: Map<Long, List<BenefitDto>>,
        rateDiscountBenefits: Map<Long, List<BenefitDto>>,
    ) {
        val benefitDtos = mutableListOf<BenefitDto>()

        // 정액 할인 혜택 추가
        fixedDiscountBenefits[id]?.let { benefitDtos.addAll(it) }

        // 기획전별 정률 할인 혜택 추가 (제외 기획전 체크)
        displayGroupIds.forEach { displayGroupId ->
            rateDiscountBenefits[displayGroupId]?.forEach { benefit ->
                if (!hasDisplayGroup(*benefit.excludeDisplayGroupIds.toLongArray())) {
                    benefitDtos.add(benefit)
                }
            }
        }

        // seq 기준 정렬
        benefitDtos.sortBy { it.seq ?: 0 }

        // 첫 번째 할인 혜택과 배송비 혜택 찾기
        var firstDiscountBenefitDto: BenefitDto? = null
        var shippingBenefitDto: BenefitDto? = null

        for (benefit in benefitDtos) {
            if (firstDiscountBenefitDto == null && benefit.type == BenefitDto.TYPE_PERIOD) {
                firstDiscountBenefitDto = benefit
            }
            if (shippingBenefitDto == null && benefit.type == BenefitDto.TYPE_DELIVERY) {
                shippingBenefitDto = benefit
            }
            if (firstDiscountBenefitDto != null && shippingBenefitDto != null) break
        }

        // 할인 혜택 적용
        firstDiscountBenefitDto?.let { benefit ->
            if (benefit.isFixedDiscountByProduct) {
                applyFixedDiscountPrice(benefit.discountValue)
            } else {
                applyDiscountRate(benefit.discountValue)
            }
            benefitEnd = benefit.end
        }

        // 배송비 혜택 설정
        shippingFeeBenefitDto = shippingBenefitDto
    }

    /**
     * 쿠폰 설정 적용
     *
     * @param couponDtoSettingsMap 기획전별 쿠폰 설정 (displayGroupId -> coupons, 0은 전체 대상)
     */
    fun applyCouponSettings(
        couponDtoSettingsMap: Map<Long, List<CouponDto>>,
        now: OffsetDateTime,
    ) {
        // 0번 (전체 상품 대상)도 포함
        val targetDisplayGroupIds = displayGroupIds.toMutableSet().apply { add(0L) }

        // 대상 쿠폰 수집
        val coupons = targetDisplayGroupIds.flatMap { couponDtoSettingsMap[it] ?: emptyList() }

        // 이미 적용된 혜택 할인 후 가격
        val discountedPrice = price - discountPrice

        // 유효한 쿠폰과 할인 금액 계산
        data class CouponWithDiscount(
            val discount: Double,
            val couponDto: CouponDto,
        )

        val validCoupons =
            coupons
                .filter { it.isValid(now, discountedPrice, targetDisplayGroupIds) }
                .map { CouponWithDiscount(it.calculateDiscount(discountedPrice), it) }
                .sortedByDescending { it.discount }

        // 그룹별 최대 할인 쿠폰만 선택
        val selectedByGroup = mutableMapOf<String, Boolean>()
        val activeCouponDtos = mutableListOf<CouponDto>()
        var totalDiscount = discountPrice

        for (couponWithDiscount in validCoupons) {
            val coupon = couponWithDiscount.couponDto
            val group = coupon.group?.lowercase()

            if (!group.isNullOrEmpty()) {
                if (selectedByGroup[group] != true) {
                    selectedByGroup[group] = true
                    activeCouponDtos.add(coupon)
                    totalDiscount += couponWithDiscount.discount
                }
            } else {
                activeCouponDtos.add(coupon)
                totalDiscount += couponWithDiscount.discount
            }
        }

        couponDtoSettings = activeCouponDtos

        // j_brand 타입은 가격 할인 없이 아이콘만 표시
        if (handleJBrandCouponPolicy()) return

        if (couponDtoSettings.isEmpty()) return

        // 할인가 적용
        applyFixedDiscountPrice(totalDiscount)

        // 쿠폰 아이콘 설정
        if (couponDtoSettings.isNotEmpty()) {
            addIcon(COUPON_ICON_NAME)
        }
    }

    /**
     * SimpleProduct로 변환
     */
    fun toSimple(): SimpleProductDto =
        SimpleProductDto(
            id = id,
            code = code,
            slug = slug,
            title = title,
            name = name,
            price = price,
            discountPrice = discountPrice,
            discountRate = discountRate,
            image = image,
            display = display,
            selling = selling,
            optionSet = optionSet,
            englishName = englishName,
            leafCategories = leafCategories,
            seller = seller,
            sellerSlug = sellerSlug,
            sellerStatus = sellerStatus,
            sellerDisplay = sellerDisplay,
            brand = brand,
            shippingFeeBenefitDto = shippingFeeBenefitDto,
            benefitEnd = benefitEnd,
            express = express,
            quantityLimit = quantityLimit,
            iconSet = iconSet,
            isOriginal = isOriginal,
            reviewCount = reviewCount,
            reviewAverage = reviewAverage,
            totalLikeCount = totalLikeCount,
        )

    // =====================================================
    // Private Helper Methods
    // =====================================================

    /**
     * j_brand 타입 상품의 쿠폰 정책 처리
     *
     * j_brand 타입은 쿠폰 아이콘만 표시하고 가격 할인은 적용하지 않음
     *
     * @return 가격 할인을 건너뛰어야 하면 true
     */
    private fun handleJBrandCouponPolicy(): Boolean {
        if (sellerDtoObject?.type == "j_brand") {
            if (couponDtoSettings.isNotEmpty()) {
                addIcon(COUPON_ICON_NAME)
            }
            return true
        }
        return false
    }
}
