package com.mediquitous.productpoc.repository.jpa.benefit.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 혜택 제외 기획전 엔티티 (읽기 전용 DAO)
 *
 * - CDC(Debezium)로 동기화되는 테이블
 * - 특정 혜택에서 제외할 기획전(DisplayGroup)을 관리
 * - Benefit과 DisplayGroup의 다대다 제외 관계 테이블
 */
@Entity
@Table(name = "shopping_benefit_exclude_displaygroups")
@Immutable
data class BenefitExcludeDisplayGroupEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 혜택 ID (FK to shopping_benefit)
    @Column(name = "benefit_id")
    val benefitId: Long? = null,
    // 기획전 ID (FK to shopping_displaygroup)
    @Column(name = "displaygroup_id")
    val displayGroupId: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BenefitExcludeDisplayGroupEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "BenefitExcludeDisplayGroupEntity(id=$id, benefitId=$benefitId, displayGroupId=$displayGroupId)"
}
