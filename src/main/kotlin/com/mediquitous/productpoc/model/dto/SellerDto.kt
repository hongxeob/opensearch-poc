package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

/**
 * 셀러 DTO
 *
 * Go 서버의 internal/service/dto/seller.go 변환
 */
data class SellerDto(
    val id: Long,
    val name: String,
    val type: String = "",
    @JsonProperty("brand_name")
    val brandName: String = "",
    @JsonProperty("influencer_name")
    val influencerName: String = "",
    val slug: String? = null,
    val segment: String? = null,
    val code: String? = null,
    @JsonProperty("profile_image")
    val profileImage: AttachmentDto? = null,
    val instagram: String? = null,
    val tiktok: String? = null,
    val status: String = "",
    @JsonProperty("is_official_brand")
    val isOfficialBrand: Boolean = false,
    val tags: List<String> = emptyList(),
    @JsonProperty("style_tags")
    val styleTags: List<String> = emptyList(),
    @JsonProperty("extra_tags")
    val extraTags: List<String> = emptyList(),
    @JsonProperty("total_like_count")
    val totalLikeCount: Long? = null,
    @JsonProperty("open_at")
    val openAt: OffsetDateTime? = null,
    val display: Boolean = false,
    @JsonProperty("new_product_begin")
    val newProductBegin: OffsetDateTime? = null,
    @JsonProperty("new_product_end")
    val newProductEnd: OffsetDateTime? = null,
    @JsonProperty("target_gender")
    val targetGender: String = "",
    val keywords: String? = null,
    @JsonProperty("keyword_array")
    val keywordArray: List<String> = emptyList(),
)
