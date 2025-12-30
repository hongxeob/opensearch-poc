package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 첨부파일(이미지) DTO
 *
 * Go 서버의 internal/service/dto/attachment.go 변환
 */
data class AttachmentDto(
    val id: Long,
    val file: String,
    @JsonProperty("original_file_name")
    val originalFileName: String? = null,
    @JsonProperty("file_size")
    val fileSize: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
)
