package com.mediquitous.productpoc.service.event.debezium

/**
 * Debezium CDC 토픽 상수 정의
 *
 * Go 서버의 internal/event/handler/debezium/topics.go 변환
 *
 * 토픽 형식: {connector-name}.{schema}.{table}
 * 예: zelda.public.shopping_product
 */
object DebeziumTopics {
    // Product 관련
    const val PRODUCT = "zelda.public.shopping_product"
    const val PRODUCT_ICON_SET = "zelda.public.shopping_product_icon_set"
    const val PRODUCT_IMAGE_SET = "zelda.public.shopping_product_image_set"
    const val PRODUCT_GUIDE_IMAGE = "zelda.public.shopping_productguideimage"
    const val PRODUCT_VARIANT = "zelda.public.shopping_productvariant"
    const val PRODUCT_VARIANT_OPTION_SET = "zelda.public.shopping_productvariant_option_set"
    const val OPTION = "zelda.public.shopping_option"
    const val PRODUCT_CATEGORY_SET = "zelda.public.shopping_product_category_set"
    const val DISPLAY_GROUP_PRODUCT = "zelda.public.shopping_displaygroupproduct"
    const val PRODUCT_BENEFIT_SET = "zelda.public.shopping_product_benefit_set"
    const val PRODUCT_RELATED_PRODUCTS = "zelda.public.shopping_product_related_products"
    const val STOCK = "zelda.public.shopping_stock"
    const val PRODUCT_BEST_ORDER = "zelda.public.shopping_productbestorder"

    // Category 관련
    const val CATEGORY = "zelda.public.shopping_category"

    // Seller 관련
    const val SELLER = "zelda.public.shopping_seller"
    const val SELLER_STAT = "zelda.public.shopping_sellerstat"

    // Like 관련
    const val LIKE = "zelda.public.shopping_like"
}
