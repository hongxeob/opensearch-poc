package com.mediquitous.productpoc.repository.jpa.customer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 고객 이벤트 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 고객의 상품 조회, 프로모션 참여 등 행동 이벤트 기록
 * - 최근 본 상품, 관심 셀러 등 추천 시스템에 활용
 */
@Entity
@Table(name = "shopping_customerevent")
@Immutable
data class CustomerEventEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 고객 ID (FK to shopping_customer)
    @Column(name = "customer_id")
    val customerId: Long? = null,
    // 상품 ID (FK to shopping_product) - nullable
    @Column(name = "product_id")
    val productId: Long? = null,
    // 프로모션 ID (FK to shopping_promotion) - nullable
    @Column(name = "promotion_id")
    val promotionId: Long? = null,
    // 이벤트 액션 (view_item, add_to_cart, purchase 등)
    @Column(name = "action", length = 200)
    val action: String? = null,
    // 이벤트 파라미터 (JSONB)
    @Column(name = "params", columnDefinition = "jsonb")
    val params: String? = null,
    // 실험 그룹 (A/B 테스트용)
    @Column(name = "experiment", length = 200)
    val experiment: String? = null,
    // 이벤트 발생 시각
    @Column(name = "created")
    val created: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomerEventEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "CustomerEventEntity(id=$id, customerId=$customerId, action=$action, productId=$productId)"
}
