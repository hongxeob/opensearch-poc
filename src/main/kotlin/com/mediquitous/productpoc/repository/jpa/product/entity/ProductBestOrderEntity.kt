package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal

/**
 * 상품 베스트 정렬 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 상품별 통계 정보 (주문수, 좋아요수, 리뷰 등)
 */
@Entity
@Table(name = "shopping_productbestorder")
@Immutable
data class ProductBestOrderEntity(
    @Id
    @Column(name = "product_id")
    val productId: Long? = null,
    // 통계 정보
    @Column(name = "order_count")
    val orderCount: Int? = null,
    @Column(name = "like_count")
    val likeCount: Int? = null,
    @Column(name = "cart_count")
    val cartCount: Int? = null,
    @Column(name = "view_count")
    val viewCount: Int? = null,
    @Column(name = "review_average", precision = 2, scale = 1)
    val reviewAverage: BigDecimal? = null,
    @Column(name = "review_count")
    val reviewCount: Int? = null,
    @Column(name = "total_like_count")
    val totalLikeCount: Int? = null,
    @Column(name = "sales_amount")
    val salesAmount: Int? = null,
    @Column(name = "discounted_price", precision = 20, scale = 2)
    val discountedPrice: BigDecimal? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductBestOrderEntity) return false
        return productId != null && productId == other.productId
    }

    override fun hashCode(): Int = productId?.hashCode() ?: 0

    override fun toString(): String = "ProductBestOrderEntity(productId=$productId, orderCount=$orderCount, likeCount=$likeCount)"
}
