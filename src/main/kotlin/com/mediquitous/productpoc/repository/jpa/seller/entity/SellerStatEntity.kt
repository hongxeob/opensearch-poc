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
 * 셀러 통계 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 셀러의 다양한 통계 지표를 기간별로 관리
 * - 범용 통계 테이블 구조 (name 필드로 통계 종류 구분)
 * - UNIQUE 제약: (LOWER(name), date, LOWER(period), seller_id)
 *
 * ## 통계 name 예시 (snake_case)
 * - order_count: 주문 건수
 * - order_amount: 주문 금액
 * - total_like_count: 총 좋아요 수
 * - review_count: 리뷰 수
 * - average_rating: 평균 평점
 *
 * ## period 타입
 * - daily: 일별
 * - weekly: 주별
 * - monthly: 월별
 * - yearly: 연별
 */
@Entity
@Table(name = "shopping_sellerstat")
@Immutable
data class SellerStatEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 셀러 ID (FK to shopping_seller)
    @Column(name = "seller_id")
    val sellerId: Long? = null,
    // 통계 이름 (snake_case, 예: order_count, total_like_count)
    @Column(name = "name", length = 200)
    val name: String? = null,
    // 통계 날짜
    @Column(name = "date")
    val date: LocalDate? = null,
    // 통계 값
    @Column(name = "value", precision = 20, scale = 2)
    val value: BigDecimal? = null,
    // 통계 기간 타입 (daily, weekly, monthly, yearly)
    @Column(name = "period", length = 20)
    val period: String? = null,
    // 업데이트 시각
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SellerStatEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "SellerStatEntity(id=$id, sellerId=$sellerId, name=$name, date=$date, period=$period, value=$value)"
}
