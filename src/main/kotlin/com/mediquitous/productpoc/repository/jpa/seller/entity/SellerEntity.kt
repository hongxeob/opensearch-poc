package com.mediquitous.productpoc.repository.jpa.seller.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * 셀러 엔티티 (읽기 전용 DAO)
 */
@Entity
@Table(name = "shopping_seller")
@Immutable
data class SellerEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 기본 정보
    @Column(name = "name", length = 255)
    val name: String? = null,
    @Column(name = "internal_name", length = 255)
    val internalName: String? = null,
    @Column(name = "nickname", length = 255)
    val nickname: String? = null,
    @Column(name = "code", length = 200)
    val code: String? = null,
    @Column(name = "slug", length = 50)
    val slug: String? = null,
    @Column(name = "type", length = 20)
    val type: String? = null,
    @Column(name = "status", length = 100)
    val status: String? = null,
    // 브랜드/인플루언서 정보
    @Column(name = "brand_name", length = 255)
    val brandName: String? = null,
    @Column(name = "brand_name_jp", length = 255)
    val brandNameJp: String? = null,
    @Column(name = "influencer_name", length = 255)
    val influencerName: String? = null,
    @Column(name = "influencer_name_jp", length = 255)
    val influencerNameJp: String? = null,
    @Column(name = "is_official_brand")
    val isOfficialBrand: Boolean? = null,
    @Column(name = "is_own_brand")
    val isOwnBrand: Boolean? = null,
    @Column(name = "is_original")
    val isOriginal: Boolean? = null,
    // 소유주 정보
    @Column(name = "owner_name", length = 255)
    val ownerName: String? = null,
    @Column(name = "owner_english_name", length = 255)
    val ownerEnglishName: String? = null,
    @Column(name = "birthday")
    val birthday: LocalDate? = null,
    // 연락처
    @Column(name = "email", length = 254)
    val email: String? = null,
    @Column(name = "phone", length = 255)
    val phone: String? = null,
    // SNS
    @Column(name = "instagram", length = 255)
    val instagram: String? = null,
    @Column(name = "tiktok", length = 255)
    val tiktok: String? = null,
    @Column(name = "line", length = 255)
    val line: String? = null,
    @Column(name = "official_site", length = 200)
    val officialSite: String? = null,
    // 설명
    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,
    @Column(name = "simple_description", length = 255)
    val simpleDescription: String? = null,
    @Column(name = "keywords", length = 500)
    val keywords: String? = null,
    // 비즈니스 정보
    @Column(name = "supplier_name", length = 200)
    val supplierName: String? = null,
    @Column(name = "supplier_type", length = 100)
    val supplierType: String? = null,
    @Column(name = "manager", length = 255)
    val manager: String? = null,
    @Column(name = "site_id", length = 255)
    val siteId: String? = null,
    // 금융 정보
    @Column(name = "commission_rate", precision = 10)
    val commissionRate: BigDecimal? = null,
    @Column(name = "commission_currency", length = 10)
    val commissionCurrency: String? = null,
    @Column(name = "nugu_ratio", precision = 10)
    val nuguRatio: BigDecimal? = null,
    @Column(name = "goal_sales", precision = 20, scale = 2)
    val goalSales: BigDecimal? = null,
    // 은행 정보
    @Column(name = "bank_account_name", length = 255)
    val bankAccountName: String? = null,
    @Column(name = "bank_account_no", length = 255)
    val bankAccountNo: String? = null,
    @Column(name = "bank_code", length = 255)
    val bankCode: String? = null,
    @Column(name = "bank_name", length = 255)
    val bankName: String? = null,
    @Column(name = "bank_branch_name", length = 255)
    val bankBranchName: String? = null,
    @Column(name = "bank_branch_address", length = 255)
    val bankBranchAddress: String? = null,
    // 회사 정보
    @Column(name = "company_name", length = 255)
    val companyName: String? = null,
    @Column(name = "company_representative", length = 255)
    val companyRepresentative: String? = null,
    @Column(name = "company_registration_no", length = 255)
    val companyRegistrationNo: String? = null,
    @Column(name = "company_business_category", length = 255)
    val companyBusinessCategory: String? = null,
    @Column(name = "business_type", length = 100)
    val businessType: String? = null,
    // 정산 정보
    @Column(name = "settlement_type", length = 100)
    val settlementType: String? = null,
    @Column(name = "settlement_period", length = 100)
    val settlementPeriod: String? = null,
    @Column(name = "settlement_period_offset")
    val settlementPeriodOffset: Int? = null,
    @Column(name = "settlement_condition", length = 100)
    val settlementCondition: String? = null,
    // 계약 정보
    @Column(name = "default_product_contract_type", length = 100)
    val defaultProductContractType: String? = null,
    // 날짜 정보
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    @Column(name = "open_at")
    val openAt: OffsetDateTime? = null,
    @Column(name = "expired_date")
    val expiredDate: OffsetDateTime? = null,
    @Column(name = "new_product_begin")
    val newProductBegin: OffsetDateTime? = null,
    @Column(name = "new_product_end")
    val newProductEnd: OffsetDateTime? = null,
    // 디스플레이
    @Column(name = "display")
    val display: Boolean? = null,
    @Column(name = "crawling_instagram")
    val crawlingInstagram: Boolean? = null,
    // 타겟 정보
    @Column(name = "target_gender", length = 100)
    val targetGender: String? = null,
    @Column(name = "segment", length = 255)
    val segment: String? = null,
    @Column(name = "language", length = 100)
    val language: String? = null,
    // 상품 승인
    @Column(name = "product_approval_type", length = 100)
    val productApprovalType: String? = null,
    // 신체 정보 (인플루언서)
    @Column(name = "height")
    val height: Int? = null,
    @Column(name = "weight")
    val weight: Int? = null,
    @Column(name = "body_frame_type", length = 20)
    val bodyFrameType: String? = null,
    @Column(name = "top_size", length = 10)
    val topSize: String? = null,
    @Column(name = "bottom_size", length = 10)
    val bottomSize: String? = null,
    @Column(name = "shoe_size", length = 10)
    val shoeSize: String? = null,
    // JSONB 필드
    @Column(name = "tags", columnDefinition = "jsonb")
    val tags: String? = null,
    @Column(name = "address", columnDefinition = "jsonb")
    val address: String? = null,
    @Column(name = "slack_id", columnDefinition = "jsonb")
    val slackId: String? = null,
    // 연관 엔티티 ID
    @Column(name = "manager_team_id")
    val managerTeamId: Long? = null,
    @Column(name = "profile_image_id")
    val profileImageId: Long? = null,
    @Column(name = "default_product_md_staff_id")
    val defaultProductMdStaffId: Long? = null,
    @Column(name = "parent_id")
    val parentId: Long? = null,
    @Column(name = "partner_id")
    val partnerId: Long? = null,
    @Column(name = "black_logo_id")
    val blackLogoId: Long? = null,
    @Column(name = "white_logo_id")
    val whiteLogoId: Long? = null,
    @Column(name = "web_designer_staff_id")
    val webDesignerStaffId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SellerEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "SellerEntity(id=$id, code=$code, name=$name, slug=$slug)"
}
