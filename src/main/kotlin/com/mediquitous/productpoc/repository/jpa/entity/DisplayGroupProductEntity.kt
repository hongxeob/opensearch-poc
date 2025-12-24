package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 기획전 상품 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 기획전(DisplayGroup)과 상품(Product)의 다대다 관계 테이블
 */
@Entity
@Table(name = "shopping_displaygroupproduct")
@Immutable
data class DisplayGroupProductEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 기획전 ID (FK to shopping_displaygroup)
    // 주의: DB 컬럼명은 group_id (displaygroup_id 아님!)
    @Column(name = "group_id")
    val groupId: Long? = null,
    // 상품 ID (FK to shopping_product)
    @Column(name = "product_id")
    val productId: Long? = null,
    // 타입
    @Column(name = "type", length = 100)
    val type: String? = null,
    // 기획전 내 상품 노출 순서
    @Column(name = "seq")
    val seq: Int? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DisplayGroupProductEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "DisplayGroupProductEntity(id=$id, groupId=$groupId, productId=$productId, seq=$seq)"
}
