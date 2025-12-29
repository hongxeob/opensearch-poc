package com.mediquitous.productpoc.model.document

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * OpenSearch Seller Document
 *
 * Go 서버의 dao/seller.go 구조를 Kotlin으로 변환
 */
data class SellerDocument(
    val id: Long,
    @JsonProperty("partner_id")
    val partnerId: Long? = null,
    val name: String = "",
    val code: String? = null,
    val type: String = "",
    @JsonProperty("target_gender")
    val targetGender: String = "",
    val slug: String? = null,
    val segment: String? = null,
    @JsonProperty("brand_name")
    val brandName: String = "",
    @JsonProperty("brand_name_jp")
    val brandNameJp: String? = null,
    val status: String = "",
    @JsonProperty("is_official_brand")
    val isOfficialBrand: Boolean = false,
    @JsonProperty("profile_image")
    val profileImage: AttachmentDocument? = null,
    val instagram: String? = null,
    val tiktok: String? = null,
    val created: Instant? = null,
    val updated: Instant? = null,
    @JsonProperty("open_at")
    val openAt: Instant? = null,
    @JsonProperty("expired_date")
    val expiredDate: Instant? = null,
    val display: Boolean = false,
    @JsonProperty("new_product_begin")
    val newProductBegin: Instant? = null,
    @JsonProperty("new_product_end")
    val newProductEnd: Instant? = null,
    @JsonProperty("influencer_name")
    val influencerName: String = "",
    @JsonProperty("influencer_name_jp")
    val influencerNameJp: String = "",
    val height: Int? = null,
    val weight: Int? = null,
    @JsonProperty("body_frame_type")
    val bodyFrameType: String? = null,
    @JsonProperty("top_size")
    val topSize: String? = null,
    @JsonProperty("bottom_size")
    val bottomSize: String? = null,
    @JsonProperty("shoe_size")
    val shoeSize: String? = null,
    @JsonProperty("style_tags")
    val styleTags: List<String> = emptyList(),
    @JsonProperty("style_tags_jp")
    val styleTagsJp: List<String> = emptyList(),
    @JsonProperty("extra_tags")
    val extraTags: List<String> = emptyList(),
    val keywords: String? = null,
    @JsonProperty("keyword_array")
    val keywordArray: List<String> = emptyList(),
    @JsonProperty("total_like_count")
    val totalLikeCount: Long? = null,
    @JsonProperty("order_count")
    val orderCount: MetricBreakdownDocument? = null,
    @JsonProperty("order_amount")
    val orderAmount: MetricBreakdownDocument? = null,
)

/**
 * 메트릭 분석 문서
 */
data class MetricBreakdownDocument(
    @JsonProperty("daily_data")
    val dailyData: List<DailyMetricDocument> = emptyList(),
)

/**
 * 일별 메트릭 문서
 */
data class DailyMetricDocument(
    val date: String,
    val value: Double,
)

/**
 * Seller Type 상수
 */
object SellerType {
    const val K_BRAND = "k_brand"
    const val J_BRAND = "j_brand"
    const val TREND_SHOPPINGMALL = "trend_shoppingmall"
    const val DIRECTOR = "director"
}

/**
 * Seller Status 상수
 */
object SellerStatus {
    const val NORMAL = "normal"
    const val CANDIDATE = "candidate"
    const val EXPIRED = "expired"
}

/**
 * Body Frame Type 상수 및 변환
 */
object BodyFrameType {
    const val STRAIGHT = "straight"
    const val NATURAL = "natural"
    const val WAVE = "wave"

    private val toJapanese =
        mapOf(
            STRAIGHT to "ストレート",
            NATURAL to "ナチュラル",
            WAVE to "ウェーブ",
        )

    fun toJapanese(type: String?): String? = type?.let { toJapanese[it] }
}
