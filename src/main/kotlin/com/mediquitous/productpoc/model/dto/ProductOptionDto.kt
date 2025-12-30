package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 상품 옵션 DTO
 *
 * Go 서버의 internal/service/dto/option.go 변환
 */
data class ProductOption(
    val id: Long,
    val name: String,
    val value: String,
    val hexcode: String? = null,
    @JsonProperty("search_name")
    val searchName: Any? = null,
    val model: Boolean = false,
)

/**
 * 옵션 그룹 DTO
 *
 * Go 서버의 internal/service/dto/option_group.go 변환
 */
data class OptionGroup(
    val name: String,
    val values: List<OptionValue> = emptyList(),
)

data class OptionValue(
    val id: Long,
    val seq: Int? = null,
    val value: String,
    val hexcode: String? = null,
)
