package com.mediquitous.productpoc.repository.jpa

import com.mediquitous.productpoc.repository.jpa.entity.AttachmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 첨부파일 JPA Repository
 *
 * zelda-product의 db/queries/attachment.sql 기반
 */
@Repository
interface AttachmentJpaRepository : JpaRepository<AttachmentEntity, Long> {
    /**
     * 여러 ID로 첨부파일 조회 (seq 순서대로)
     *
     * SQL: GetAttachmentByIDs
     */
    @Query(
        """
        SELECT a FROM AttachmentEntity a
        WHERE a.id IN :ids
        ORDER BY a.seq
    """,
    )
    fun findByIds(
        @Param("ids") ids: List<Long>,
    ): List<AttachmentEntity>

    /**
     * 상품 ID로 첨부파일 조회 (ProductImageSet JOIN)
     *
     * SQL: GetAttachmentByProductID
     */
    @Query(
        """
        SELECT a FROM AttachmentEntity a
        JOIN ProductImageSetEntity pis ON pis.attachmentId = a.id
        WHERE pis.productId = :productId
        ORDER BY a.seq
    """,
    )
    fun findByProductId(
        @Param("productId") productId: Long,
    ): List<AttachmentEntity>
}
