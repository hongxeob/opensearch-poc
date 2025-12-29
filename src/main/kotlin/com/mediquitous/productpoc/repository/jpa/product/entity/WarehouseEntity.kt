package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 창고 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 빠른배송 여부 정보 포함
 */
@Entity
@Table(name = "shopping_warehouse")
@Immutable
data class WarehouseEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 창고명
    @Column(name = "name", nullable = false, length = 255)
    val name: String? = null,
    // 빠른배송 여부
    @Column(name = "quick", nullable = false)
    val quick: Boolean? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WarehouseEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "WarehouseEntity(id=$id, name=$name, quick=$quick)"
}
