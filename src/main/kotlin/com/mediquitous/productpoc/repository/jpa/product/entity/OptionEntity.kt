package com.mediquitous.productpoc.repository.jpa.product.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 상품 옵션 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 상품의 옵션 정보 (색상, 사이즈 등)
 */
@Entity
@Table(name = "shopping_option")
@Immutable
data class OptionEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 상품 ID (FK)
    @Column(name = "product_id")
    val productId: Long? = null,
    // 옵션 정보
    @Column(name = "name", length = 200)
    val name: String? = null,
    @Column(name = "value", length = 200)
    val value: String? = null,
    @Column(name = "value_code", length = 200)
    val valueCode: String? = null,
    // 색상 헥사코드
    @Column(name = "hexcode", length = 20)
    val hexcode: String? = null,
    // 검색용 색상명
    @Column(name = "search_name", length = 200)
    val searchName: String? = null,
    // 모델 착용 여부
    @Column(name = "model")
    val model: Boolean? = null,
    // 정렬 순서
    @Column(name = "name_seq")
    val nameSeq: Int? = null,
    @Column(name = "value_seq")
    val valueSeq: Int? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OptionEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "OptionEntity(id=$id, productId=$productId, name=$name)"
}
