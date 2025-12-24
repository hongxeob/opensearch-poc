package com.mediquitous.productpoc.repository.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 상품 옵션(Variant) 엔티티 (읽기 전용 DAO)
 */
@Entity
@Table(name = "shopping_productvariant")
@Immutable
data class ProductVariantEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "code", length = 255)
    val code: String? = null,
    @Column(name = "barcode", length = 200)
    val barcode: String? = null,
    @Column(name = "external_barcode", length = 200)
    val externalBarcode: String? = null,
    @Column(name = "barcode2", length = 255)
    val barcode2: String? = null,
    @Column(name = "display")
    val display: OffsetDateTime? = null,
    @Column(name = "selling")
    val selling: OffsetDateTime? = null,
    @Column(name = "deleted")
    val deleted: OffsetDateTime? = null,
    @Column(name = "options", columnDefinition = "jsonb")
    val options: String? = null,
    @Column(name = "additional_price", precision = 20, scale = 2)
    val additionalPrice: BigDecimal? = null,
    @Column(name = "base_currency_price", precision = 20, scale = 2)
    val baseCurrencyPrice: BigDecimal? = null,
    @Column(name = "supply_price", precision = 20, scale = 2)
    val supplyPrice: BigDecimal? = null,
    @Column(name = "use_inventory")
    val useInventory: Boolean? = null,
    @Column(name = "display_soldout")
    val displaySoldout: Boolean? = null,
    @Column(name = "inventory_type", length = 100)
    val inventoryType: String? = null,
    @Column(name = "quantity_check_type", length = 100)
    val quantityCheckType: String? = null,
    @Column(name = "quantity")
    val quantity: Int? = null,
    @Column(name = "safety_quantity")
    val safetyQuantity: Int? = null,
    @Column(name = "scm_hash", length = 100)
    val scmHash: String? = null,
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    @Column(name = "product_id")
    val productId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductVariantEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "ProductVariantEntity(id=$id, code=$code, barcode=$barcode)"
}
