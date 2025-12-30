package com.mediquitous.productpoc.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.floor

/**
 * 쿠폰 설정 DTO
 *
 * Go 서버의 internal/service/dto/coupon.go 변환
 */
data class CouponDto(
    val id: Long,
    val code: String,
    val name: String,
    val description: String? = null,
    val group: String? = null,
    @JsonProperty("benefit_type")
    val benefitType: String,
    @JsonProperty("benefit_value")
    val benefitValue: Double = 0.0,
    @JsonProperty("benefit_max_value")
    val benefitMaxValue: Double? = null,
    @JsonProperty("issue_type")
    val issueType: String,
    @JsonProperty("issue_max_count")
    val issueMaxCount: Int? = null,
    @JsonProperty("issue_max_count_by_user")
    val issueMaxCountByUser: Int = 0,
    @JsonProperty("available_min_price")
    val availableMinPrice: Double = 0.0,
    @JsonProperty("available_coupon_count_by_order")
    val availableCouponCountByOrder: Int = 0,
    @JsonProperty("available_period_type")
    val availablePeriodType: String,
    @JsonProperty("available_begin")
    val availableBegin: OffsetDateTime? = null,
    @JsonProperty("available_end")
    val availableEnd: OffsetDateTime? = null,
    @JsonProperty("available_day_from_issued")
    val availableDayFromIssued: Int? = null,
    @JsonProperty("show_product_detail")
    val showProductDetail: Boolean = false,
    val deleted: OffsetDateTime? = null,
    val type: String,
    @JsonProperty("benefit_percentage_round_unit")
    val benefitPercentageRoundUnit: Double? = null,
    @JsonProperty("amount_type")
    val amountType: String? = null,
    @JsonProperty("issued_count")
    val issuedCount: Int = 0,
    @JsonProperty("include_regional_shipping_rate")
    val includeRegionalShippingRate: Boolean = false,
    @JsonProperty("available_platform")
    val availablePlatform: String = "",
    @JsonProperty("issue_begin")
    val issueBegin: OffsetDateTime,
    @JsonProperty("issue_end")
    val issueEnd: OffsetDateTime,
    @JsonProperty("is_paused")
    val isPaused: Boolean = false,
    @JsonProperty("target_type")
    val targetType: String = "",
    @JsonProperty("auto_issue_type")
    val autoIssueType: String? = null,
    @JsonProperty("available_fixed_date_begin")
    val availableFixedDateBegin: LocalDate? = null,
    @JsonProperty("available_fixed_date_end")
    val availableFixedDateEnd: LocalDate? = null,
    @JsonProperty("available_fixed_time_begin")
    val availableFixedTimeBegin: LocalTime? = null,
    @JsonProperty("available_fixed_time_end")
    val availableFixedTimeEnd: LocalTime? = null,
    val displaygroup: Long? = null,
    @JsonProperty("exclude_displaygroup")
    val excludeDisplaygroup: Long? = null,
) {
    companion object {
        // 쿠폰 타입 상수
        const val TYPE_ONLINE = "online"
        const val TYPE_OFFLINE = "offline"

        // 타겟 타입 상수
        const val TARGET_TYPE_ALL = "all"
        const val TARGET_TYPE_GROUP = "group"
        const val TARGET_TYPE_SPECIFIC = "specific"

        // 혜택 타입 상수
        const val BENEFIT_TYPE_RATE = "rate" // 상품할인쿠폰 (%)
        const val BENEFIT_TYPE_AMOUNT = "amount" // 주문서금액할인쿠폰 (정액)
        const val BENEFIT_TYPE_DELIVERY = "delivery" // 배송비할인쿠폰

        // 발급 타입 상수
        const val ISSUE_TYPE_DOWNLOAD = "download"
        const val ISSUE_TYPE_AUTO = "auto"
        const val ISSUE_TYPE_ADMIN = "admin"

        // 사용 가능 기간 타입 상수
        const val PERIOD_TYPE_END_OF_MONTH = "end_of_month"
        const val PERIOD_TYPE_PERIOD = "period"
        const val PERIOD_TYPE_DAY = "day"
        const val PERIOD_TYPE_TIME_OF_THE_DAY = "time_of_the_day"
        const val PERIOD_TYPE_FIXED_DURATION = "fixed_duration"

        // 도쿄 시간대
        val ASIA_TOKYO_ZONE: ZoneId = ZoneId.of("Asia/Tokyo")
    }

    /**
     * 쿠폰 유효성 검사
     *
     * @param now 현재 시간
     * @param price 할인 적용 후 가격
     * @param displayGroupIds 상품이 속한 기획전 ID 목록
     * @return 유효 여부
     */
    fun isValid(
        now: OffsetDateTime,
        price: Double,
        displayGroupIds: Set<Long>,
    ): Boolean {
        if (!isDefaultValid(now, price)) return false
        if (!isAvailablePeriodValid(now)) return false
        if (!isDisplayGroupValid(displayGroupIds)) return false
        return true
    }

    /**
     * 쿠폰 할인 금액 계산
     *
     * @param price 할인 적용 전 가격
     * @return 할인 금액
     */
    fun calculateDiscount(price: Double): Double {
        val discount =
            when (benefitType) {
                BENEFIT_TYPE_RATE -> floor(price * benefitValue / 100)
                BENEFIT_TYPE_AMOUNT -> benefitValue
                else -> 0.0
            }

        // 최대 할인 금액 제한
        if (benefitMaxValue != null && benefitMaxValue > 0 && discount > benefitMaxValue) {
            return benefitMaxValue
        }

        return discount
    }

    // =====================================================
    // Private Validation Methods
    // =====================================================

    private fun isDefaultValid(
        now: OffsetDateTime,
        price: Double,
    ): Boolean {
        // 삭제 여부
        if (deleted != null) return false

        // 일시 중지 여부
        if (isPaused) return false

        // 쿠폰 타입 (온라인만)
        if (type != TYPE_ONLINE) return false

        // 발급 타입 (다운로드만)
        if (issueType != ISSUE_TYPE_DOWNLOAD) return false

        // 사용 가능 최소 금액
        if (price <= availableMinPrice) return false

        // 최대 발급 수
        if (issueMaxCount != null && issuedCount >= issueMaxCount) return false

        // 발급 기간
        if (issueBegin.isAfter(now) || issueEnd.isBefore(now)) return false

        return true
    }

    private fun isAvailablePeriodValid(now: OffsetDateTime): Boolean {
        return when (availablePeriodType) {
            PERIOD_TYPE_END_OF_MONTH, PERIOD_TYPE_DAY -> {
                // 발급 후 바로 사용 가능하므로 검사 제외
                true
            }

            PERIOD_TYPE_PERIOD -> {
                if (availableBegin == null || availableEnd == null) return false
                !(availableBegin.isAfter(now) || availableEnd.isBefore(now))
            }

            PERIOD_TYPE_TIME_OF_THE_DAY -> {
                if (availableBegin == null || availableEnd == null) return false
                val nowTime = now.atZoneSameInstant(ASIA_TOKYO_ZONE).toLocalTime()
                val beginTime = availableBegin.atZoneSameInstant(ASIA_TOKYO_ZONE).toLocalTime()
                val endTime = availableEnd.atZoneSameInstant(ASIA_TOKYO_ZONE).toLocalTime()
                !(nowTime.isBefore(beginTime) || !nowTime.isBefore(endTime))
            }

            PERIOD_TYPE_FIXED_DURATION -> {
                if (availableFixedDateBegin == null || availableFixedDateEnd == null ||
                    availableFixedTimeBegin == null || availableFixedTimeEnd == null
                ) {
                    return false
                }

                val nowDate = now.toLocalDate()
                if (nowDate.isBefore(availableFixedDateBegin) || nowDate.isAfter(availableFixedDateEnd)) {
                    return false
                }

                val nowTime = now.atZoneSameInstant(ASIA_TOKYO_ZONE).toLocalTime()
                !(nowTime.isBefore(availableFixedTimeBegin) || !nowTime.isBefore(availableFixedTimeEnd))
            }

            else -> {
                false
            }
        }
    }

    private fun isDisplayGroupValid(displayGroupIds: Set<Long>): Boolean {
        // 전체 상품 대상 쿠폰
        if (displaygroup == null) return true

        // 해당 기획전에 속해있는지 확인
        if (displaygroup !in displayGroupIds) return false

        // 제외 기획전 확인
        if (excludeDisplaygroup != null && excludeDisplaygroup in displayGroupIds) {
            return false
        }

        return true
    }
}
