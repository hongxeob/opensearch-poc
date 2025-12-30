package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 가이드 이미지 DTO
 *
 * Go 서버의 internal/service/dto/guide_image.go 변환
 */
data class GuideImageDto(
    val id: Long,
    val file: String? = null,
    val type: String? = null,
    @JsonProperty("image_map")
    val imageMap: Map<String, Any>? = null,
)
