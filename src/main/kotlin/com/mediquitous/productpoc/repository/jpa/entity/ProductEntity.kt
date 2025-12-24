package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 상품 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 모든 필드 nullable (CDC 이벤트 필드 누락 대비)
 * - 연관관계는 ID만 사용 (조인 없이 조회)
 */
@Entity
@Table(name = "shopping_product")
@Immutable
data class ProductEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 기본 정보
    @Column(name = "code", length = 200)
    val code: String? = null,
    @Column(name = "name", length = 200)
    val name: String? = null,
    @Column(name = "supply_name", length = 200)
    val supplyName: String? = null,
    @Column(name = "internal_name", length = 200)
    val internalName: String? = null,
    @Column(name = "model_name", length = 200)
    val modelName: String? = null,
    @Column(name = "english_name", length = 200)
    val englishName: String? = null,
    @Column(name = "supply_name_cn", length = 200)
    val supplyNameCn: String? = null,
    // 가격 정보
    @Column(name = "price", precision = 20, scale = 2)
    val price: BigDecimal? = null,
    @Column(name = "price_before_tax", precision = 20, scale = 2)
    val priceBeforeTax: BigDecimal? = null,
    @Column(name = "retail_price", precision = 20, scale = 2)
    val retailPrice: BigDecimal? = null,
    @Column(name = "supply_price", precision = 20, scale = 2)
    val supplyPrice: BigDecimal? = null,
    @Column(name = "base_currency_price", precision = 20, scale = 2)
    val baseCurrencyPrice: BigDecimal? = null,
    // 날짜 정보
    @Column(name = "display")
    val display: OffsetDateTime? = null,
    @Column(name = "selling")
    val selling: OffsetDateTime? = null,
    @Column(name = "released")
    val released: OffsetDateTime? = null,
    @Column(name = "deleted")
    val deleted: OffsetDateTime? = null,
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    // 상세 정보
    @Column(name = "title", length = 300)
    val title: String? = null,
    @Column(name = "annotation", columnDefinition = "TEXT")
    val annotation: String? = null,
    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,
    @Column(name = "memo", columnDefinition = "TEXT")
    val memo: String? = null,
    @Column(name = "size_info", columnDefinition = "TEXT")
    val sizeInfo: String? = null,
    // 옵션 및 설정
    @Column(name = "option_type", length = 100)
    val optionType: String? = null,
    @Column(name = "quantity_limit")
    val quantityLimit: Int? = null,
    @Column(name = "quantity_limit_type", length = 20)
    val quantityLimitType: String? = null,
    @Column(name = "member_only")
    val memberOnly: Boolean? = null,
    @Column(name = "repurchasable")
    val repurchasable: Boolean? = null,
    @Column(name = "is_auto_pricing")
    val isAutoPricing: Boolean? = null,
    // 물류 정보
    @Column(name = "weight", precision = 20, scale = 2)
    val weight: BigDecimal? = null,
    @Column(name = "material", length = 255)
    val material: String? = null,
    @Column(name = "cloth_fabric", length = 100)
    val clothFabric: String? = null,
    // 통관 정보
    @Column(name = "clearance_category_eng", length = 100)
    val clearanceCategoryEng: String? = null,
    @Column(name = "clearance_category_kor", length = 100)
    val clearanceCategoryKor: String? = null,
    @Column(name = "clearance_category_code", length = 10)
    val clearanceCategoryCode: String? = null,
    // 비율 및 수치
    @Column(name = "margin_rate", precision = 20, scale = 2)
    val marginRate: BigDecimal? = null,
    @Column(name = "tax_rate", precision = 10, scale = 2)
    val taxRate: BigDecimal? = null,
    @Column(name = "commission_rate", precision = 10)
    val commissionRate: BigDecimal? = null,
    // 계약 정보
    @Column(name = "contract_type", length = 30)
    val contractType: String? = null,
    @Column(name = "commission_currency", length = 10)
    val commissionCurrency: String? = null,
    // SCM 정보
    @Column(name = "scm_id", length = 200)
    val scmId: String? = null,
    @Column(name = "scm_hash", length = 100)
    val scmHash: String? = null,
    // 관리 정보
    @Column(name = "slug", length = 255)
    val slug: String? = null,
    @Column(name = "custom_code", length = 200)
    val customCode: String? = null,
    @Column(name = "season", length = 30)
    val season: String? = null,
    @Column(name = "use_days", precision = 20, scale = 2)
    val useDays: BigDecimal? = null,
    // JSONB 필드
    @Column(name = "point_settings", columnDefinition = "jsonb")
    val pointSettings: String? = null,
    @Column(name = "info", columnDefinition = "jsonb")
    val info: String? = null,
    // 연관 엔티티 ID (조인 없이 ID만 저장)
    @Column(name = "classification_id")
    val classificationId: Long? = null,
    @Column(name = "supplier_id")
    val supplierId: Long? = null,
    @Column(name = "manufacturer_id")
    val manufacturerId: Long? = null,
    @Column(name = "origin_id")
    val originId: Long? = null,
    @Column(name = "brand_id")
    val brandId: Long? = null,
    @Column(name = "trend_id")
    val trendId: Long? = null,
    @Column(name = "seller_id")
    val sellerId: Long? = null,
    @Column(name = "image_id")
    val imageId: Long? = null,
    @Column(name = "md_staff_id")
    val mdStaffId: Long? = null,
    @Column(name = "web_staff_id")
    val webStaffId: Long? = null,
    @Column(name = "hscode_id")
    val hscodeId: Long? = null,
    @Column(name = "guide_image_id")
    val guideImageId: Long? = null,
    @Column(name = "standard_category_id")
    val standardCategoryId: Long? = null,
) {
    /**
     * equals/hashCode는 ID 기반
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    /**
     * toString()에서 JSONB 필드 제외 (로깅 간소화)
     */
    override fun toString(): String = "ProductEntity(id=$id, code=$code, name=$name)"
}
