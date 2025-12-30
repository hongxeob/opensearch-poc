package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 커서 기반 페이지네이션 응답
 */
@Schema(description = "커서 기반 페이지네이션 응답")
data class CursorPaginationResponse<T>(
    @Schema(description = "전체 결과 개수", example = "100")
    val count: Long,
    @Schema(description = "결과 목록")
    val results: List<T>,
    @Schema(description = "다음 페이지 커서")
    @JsonProperty("next")
    val nextCursor: String?,
    @Schema(description = "이전 페이지 커서")
    @JsonProperty("previous")
    val previousCursor: String?,
) {
    companion object {
        fun <T> empty() =
            CursorPaginationResponse<T>(
                count = 0,
                results = emptyList(),
                nextCursor = null,
                previousCursor = null,
            )
    }
}
