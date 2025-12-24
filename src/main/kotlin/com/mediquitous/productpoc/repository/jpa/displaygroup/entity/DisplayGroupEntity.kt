package com.mediquitous.productpoc.repository.jpa.displaygroup.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 기획전 엔티티 (읽기 전용 DAO)
 *
 * - DisplayGroupProduct에서 group_id로 참조
 * - 기획전(프로모션) 기본 정보
 * - 주의: 실제 스키마에는 50개 이상의 필드가 있지만, 필수 필드만 정의
 */
@Entity
@Table(name = "shopping_displaygroup")
@Immutable
data class DisplayGroupEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    // 기본 정보
    @Column(name = "name", length = 255)
    val name: String? = null,
    @Column(name = "code", length = 255)
    val code: String? = null,
    @Column(name = "internal_name", length = 255)
    val internalName: String? = null,
    @Column(name = "subtitle", length = 255)
    val subtitle: String? = null,
    @Column(name = "content", columnDefinition = "TEXT")
    val content: String? = null,
    // 타입 및 설정
    @Column(name = "type", length = 100)
    val type: String? = null,
    @Column(name = "soldout_sort_type", length = 100)
    val soldoutSortType: String? = null,
    @Column(name = "type_auto_ordering", length = 100)
    val typeAutoOrdering: String? = null,
    @Column(name = "show_product_type", length = 100)
    val showProductType: String? = null,
    @Column(name = "child_display", length = 100)
    val childDisplay: String? = null,
    @Column(name = "name_position", length = 20)
    val namePosition: String? = null,
    @Column(name = "thumbnail_location", length = 20)
    val thumbnailLocation: String? = null,
    @Column(name = "component", length = 200)
    val component: String? = null,
    // 순서 및 크기
    @Column(name = "seq")
    val seq: Int? = null,
    @Column(name = "auto_size")
    val autoSize: Int? = null,
    // URL
    @Column(name = "origin_url", length = 200)
    val originUrl: String? = null,
    @Column(name = "redirect_url", length = 100)
    val redirectUrl: String? = null,
    // 기간
    @Column(name = "begin")
    val begin: OffsetDateTime? = null,
    @Column(name = "end")
    val end: OffsetDateTime? = null,
    @Column(name = "commentable")
    val commentable: OffsetDateTime? = null,
    // 플래그
    @Column(name = "type_auto_express")
    val typeAutoExpress: Boolean? = null,
    @Column(name = "type_auto_subdisplaygroup")
    val typeAutoSubdisplaygroup: Boolean? = null,
    @Column(name = "show_countdown")
    val showCountdown: Boolean? = null,
    // JSONB
    @Column(name = "banner_groups", columnDefinition = "jsonb")
    val bannerGroups: String? = null,
    // 연관 ID
    @Column(name = "thumbnail_id")
    val thumbnailId: Long? = null,
    @Column(name = "video_id")
    val videoId: Long? = null,
    @Column(name = "manager_team_id")
    val managerTeamId: Long? = null,
    @Column(name = "parent_id")
    val parentId: Long? = null,
    @Column(name = "md_staff_id")
    val mdStaffId: Long? = null,
    @Column(name = "web_staff_id")
    val webStaffId: Long? = null,
    @Column(name = "seller_id")
    val sellerId: Long? = null,
    // 날짜 정보
    @Column(name = "created")
    val created: OffsetDateTime? = null,
    @Column(name = "updated")
    val updated: OffsetDateTime? = null,
    @Column(name = "deleted")
    val deleted: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DisplayGroupEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "DisplayGroupEntity(id=$id, code=$code, name=$name)"
}
