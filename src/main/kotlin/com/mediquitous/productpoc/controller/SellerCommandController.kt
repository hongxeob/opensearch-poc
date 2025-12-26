package com.mediquitous.productpoc.controller

import com.mediquitous.productpoc.service.SellerMigrationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * 셀러 명령 컨트롤러
 *
 * Go 서버의 셀러 마이그레이션 API를 담당
 */
@Tag(name = "Sellers - Commands", description = "셀러 명령 API (마이그레이션)")
@RestController
@RequestMapping("/api/v1/sellers")
class SellerCommandController(
    private val sellerMigrationService: SellerMigrationService,
) {
    @Operation(
        summary = "전체 셀러 마이그레이션",
        description = "PostgreSQL에서 OpenSearch로 전체 셀러를 마이그레이션합니다",
    )
    @PostMapping("/migrate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun migrateAllSellers() {
        sellerMigrationService.migrateAll()
    }
}
