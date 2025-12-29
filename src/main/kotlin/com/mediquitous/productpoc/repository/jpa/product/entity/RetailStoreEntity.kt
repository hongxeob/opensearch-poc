package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 오프라인 매장 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - Warehouse와 1:1 관계
 */
@Entity
@Table(name = "shopping_retailstore")
@Immutable
data class RetailStoreEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 매장명
    @Column(name = "name", nullable = false, length = 100)
    val name: String? = null,
    // 위치
    @Column(name = "location", nullable = false, length = 100)
    val location: String? = null,
    // 주소
    @Column(name = "address", nullable = false, length = 255)
    val address: String? = null,
    // 전화번호
    @Column(name = "phone", nullable = false, length = 100)
    val phone: String? = null,
    // 영업시간
    @Column(name = "business_hours", nullable = false, length = 100)
    val businessHours: String? = null,
    // 창고 ID (FK)
    @Column(name = "warehouse_id", nullable = false)
    val warehouseId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RetailStoreEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "RetailStoreEntity(id=$id, name=$name, warehouseId=$warehouseId)"
}
