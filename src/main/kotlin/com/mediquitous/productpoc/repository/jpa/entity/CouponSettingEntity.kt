package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * 쿠폰 설정 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 쿠폰의 발급 및 사용 조건 설정 정보
 * - soft delete 지원 (deleted 컬럼)
 */
@Entity
@Table(name = "shopping_couponsetting")
@Immutable
data class CouponSettingEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 쿠폰 기본 정보
    @Column(name = "code", length = 255)
    val code: String? = null,
    @Column(name = "type", length = 100)
    val type: String? = null,
    @Column(name = "name", length = 255)
    val name: String? = null,
    @Column(name = "description", length = 255)
    val description: String? = null,
    // 혜택 정보
    @Column(name = "benefit_type", length = 255)
    val benefitType: String? = null,
    @Column(name = "benefit_value", precision = 20, scale = 2)
    val benefitValue: BigDecimal? = null,
    @Column(name = "benefit_max_value", precision = 20, scale = 2)
    val benefitMaxValue: BigDecimal? = null,
    @Column(name = "benefit_percentage_round_unit")
    val benefitPercentageRoundUnit: Double? = null,
    @Column(name = "amount_type", length = 255)
    val amountType: String? = null,
    @Column(name = "include_regional_shipping_rate")
    val includeRegionalShippingRate: Boolean? = null,
    // 발급 조건
    @Column(name = "issue_type", length = 255)
    val issueType: String? = null,
    @Column(name = "issue_member_join_type", length = 1)
    val issueMemberJoinType: String? = null,
    @Column(name = "issue_max_count")
    val issueMaxCount: Int? = null,
    @Column(name = "issue_max_count_by_user")
    val issueMaxCountByUser: Int? = null,
    @Column(name = "issued_count")
    val issuedCount: Int? = null,
    @Column(name = "issue_begin")
    val issueBegin: OffsetDateTime? = null,
    @Column(name = "issue_end")
    val issueEnd: OffsetDateTime? = null,
    @Column(name = "issue_reserved")
    val issueReserved: OffsetDateTime? = null,
    @Column(name = "auto_issue_type", length = 20)
    val autoIssueType: String? = null,
    @Column(name = "is_paused")
    val isPaused: Boolean? = null,
    @Column(name = "pause_begin")
    val pauseBegin: OffsetDateTime? = null,
    @Column(name = "pause_end")
    val pauseEnd: OffsetDateTime? = null,
    // 사용 조건
    @Column(name = "available_period_type", length = 255)
    val availablePeriodType: String? = null,
    @Column(name = "available_begin")
    val availableBegin: OffsetDateTime? = null,
    @Column(name = "available_end")
    val availableEnd: OffsetDateTime? = null,
    @Column(name = "available_day_from_issued")
    val availableDayFromIssued: Int? = null,
    @Column(name = "available_fixed_date_begin")
    val availableFixedDateBegin: LocalDate? = null,
    @Column(name = "available_fixed_date_end")
    val availableFixedDateEnd: LocalDate? = null,
    @Column(name = "available_fixed_time_begin")
    val availableFixedTimeBegin: LocalTime? = null,
    @Column(name = "available_fixed_time_end")
    val availableFixedTimeEnd: LocalTime? = null,
    @Column(name = "available_site", length = 255)
    val availableSite: String? = null,
    @Column(name = "available_platform", length = 20)
    val availablePlatform: String? = null,
    @Column(name = "available_price_type", length = 20)
    val availablePriceType: String? = null,
    @Column(name = "available_order_price_type", length = 1)
    val availableOrderPriceType: String? = null,
    @Column(name = "available_min_price", precision = 20, scale = 2)
    val availableMinPrice: BigDecimal? = null,
    @Column(name = "available_coupon_count_by_order")
    val availableCouponCountByOrder: Int? = null,
    // 대상 설정
    @Column(name = "target_type", length = 10)
    val targetType: String? = null,
    @Column(name = "customer_group_id")
    val customerGroupId: Long? = null,
    @Column(name = "displaygroup_id")
    val displayGroupId: Long? = null,
    @Column(name = "exclude_displaygroup_id")
    val excludeDisplayGroupId: Long? = null,
    // 표시 및 알림
    @Column(name = "show_product_detail")
    val showProductDetail: Boolean? = null,
    @Column(name = "use_notification_when_login")
    val useNotificationWhenLogin: Boolean? = null,
    @Column(name = "send_sms_for_issue")
    val sendSmsForIssue: Boolean? = null,
    @Column(name = "send_email_for_issue")
    val sendEmailForIssue: Boolean? = null,
    // 중복 사용 방지
    @Column(name = "group", length = 255)
    val group: String? = null,
    // 날짜 정보
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    @Column(name = "deleted")
    val deleted: OffsetDateTime? = null,
) {
    /**
     * 활성화 상태 여부
     */
    val isActive: Boolean
        get() = deleted == null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CouponSettingEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "CouponSettingEntity(id=$id, code=$code, name=$name, type=$type)"
}
