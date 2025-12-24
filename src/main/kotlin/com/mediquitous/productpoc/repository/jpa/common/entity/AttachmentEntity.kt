package com.mediquitous.productpoc.repository.jpa.common.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.OffsetDateTime

/**
 * 첨부파일(이미지) 엔티티 (읽기 전용 DAO)
 */
@Entity
@Table(name = "shopping_attachment")
@Immutable
data class AttachmentEntity(
    @Id
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "mimetype", length = 200)
    val mimetype: String? = null,
    @Column(name = "file", length = 100)
    val file: String? = null,
    @Column(name = "filename", length = 200)
    val filename: String? = null,
    @Column(name = "url", length = 200)
    val url: String? = null,
    @Column(name = "position", length = 100)
    val position: String? = null,
    @Column(name = "seq")
    val seq: Int? = null,
    @Column(name = "display")
    val display: Boolean? = null,
    @Column(name = "created")
    val created: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AttachmentEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "AttachmentEntity(id=$id, filename=$filename)"
}
