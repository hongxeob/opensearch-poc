package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 카테고리 DTO (응답용)
 *
 * Go 서버의 internal/service/dto/category.go 변환
 */
data class CategoryDto(
    val id: Long,
    @JsonProperty("parent_id")
    val parentId: Long? = null,
    val name: String,
    @JsonProperty("display_name")
    val displayName: String,
    val slug: String? = null,
    @JsonProperty("is_visible")
    val isVisible: Boolean = true,
    @JsonProperty("is_leaf")
    val isLeaf: Boolean = true,
)
