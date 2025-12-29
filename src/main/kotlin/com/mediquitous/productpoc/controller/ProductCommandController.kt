package com.mediquitous.productpoc.controller

import com.mediquitous.productpoc.service.migration.ProductMigrationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 상품 명령 컨트롤러
 *
 * Go 서버의 마이그레이션 및 flush 관련 API를 담당
 */
@Tag(name = "Products - Commands", description = "상품 명령 API (마이그레이션, flush)")
@RestController
@RequestMapping("/api/v1/products")
class ProductCommandController(
    private val productMigrationService: ProductMigrationService,
) {
    @Operation(
        summary = "이벤트 버퍼 Flush",
        description = "지정된 개수만큼 이벤트 버퍼를 flush합니다",
    )
    @PostMapping("/flush")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun flushEventBuffer(
        @Parameter(description = "Flush할 이벤트 개수", required = true)
        @RequestBody request: FlushRequest,
    ) {
        productMigrationService.flushEventBuffer(request.count)
    }

    @Operation(
        summary = "전체 상품 마이그레이션",
        description = "PostgreSQL에서 OpenSearch로 전체 상품을 마이그레이션합니다",
    )
    @PostMapping("/migrate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun migrateAllProducts() {
        productMigrationService.migrateAll()
    }

    @Operation(
        summary = "특정 상품 마이그레이션",
        description = "지정된 상품 ID 목록을 PostgreSQL에서 OpenSearch로 마이그레이션합니다",
    )
    @PostMapping("/migrate-by-ids")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun migrateProductsByIds(
        @Parameter(description = "마이그레이션할 상품 ID 목록", required = true)
        @RequestBody productIds: List<Long>,
    ) {
        productMigrationService.migrateByIds(productIds)
    }

    data class FlushRequest(
        val count: Int,
    )
}
