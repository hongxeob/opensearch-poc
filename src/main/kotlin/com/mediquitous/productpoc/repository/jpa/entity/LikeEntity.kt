package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 좋아요 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 고객의 상품/셀러 좋아요 정보
 */
@Entity
@Table(name = "shopping_like")
@Immutable
data class LikeEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 좋아요 대상 타입
    @Column(name = "target", length = 30)
    val target: String? = null,
    // 고객 ID (FK to shopping_customer)
    @Column(name = "customer_id")
    val customerId: Long? = null,
    // 상품 ID (FK to shopping_product) - nullable
    @Column(name = "product_id")
    val productId: Long? = null,
    // 셀러 ID (FK to shopping_seller) - nullable
    @Column(name = "seller_id")
    val sellerId: Long? = null,
    // 스타일룩 ID - nullable
    @Column(name = "style_look_id")
    val styleLookId: Long? = null,
    // 앰버서더 ID - nullable
    @Column(name = "ambassador_id")
    val ambassadorId: Long? = null,
    // 생성일
    @Column(name = "created")
    val created: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LikeEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "LikeEntity(id=$id, target=$target, customerId=$customerId, productId=$productId, sellerId=$sellerId)"
}
