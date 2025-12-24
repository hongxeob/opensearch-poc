package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 재고 엔티티 (읽기 전용 DAO)
 */
@Entity
@Table(name = "shopping_stock")
@Immutable
data class StockEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "quantity")
    val quantity: Int? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    @Column(name = "product_variant_id")
    val productVariantId: Long? = null,
    @Column(name = "warehouse_id")
    val warehouseId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StockEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "StockEntity(id=$id, quantity=$quantity)"
}
