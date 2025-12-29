package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.ProductGuideImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * 가이드 이미지 + 첨부파일 JOIN 결과
 * Go의 GetProductGuideImageByIDsRow에 해당
 */
interface GuideImageWithAttachment {
    // shopping_productguideimage
    val id: Long
    val name: String?

    // shopping_attachment (image_id FK)
    val attachmentId: Long?
    val mimetype: String?
    val file: String?
    val seq: Int?
}

/**
 * 상품 가이드 이미지 JPA Repository
 *
 * zelda-product의 db/queries/productguideimage.sql 기반
 * - GetProductGuideImageByIDs: ID로 가이드 이미지 조회 (Attachment JOIN)
 *
 * Note: Go SQL에서는 Attachment를 함께 JOIN하지만,
 * Spring에서는 Entity의 연관관계 매핑(@ManyToOne)을 통해 처리합니다.
 */
@Repository
interface ProductGuideImageJpaRepository : JpaRepository<ProductGuideImageEntity, Long> {
    /**
     * ID로 가이드 이미지 조회
     *
     * SQL: GetProductGuideImageByIDs
     * Spring Data JPA의 기본 메서드 findById()를 사용하거나 nullable 조회
     */
    fun findNullableById(id: Long): ProductGuideImageEntity?

    @Query(
        """
        SELECT 
            g.id as id,
            g.name as name,
            a.id as attachmentId,
            a.mimetype as mimetype,
            a.file as file,
            a.seq as seq
        FROM ProductGuideImageEntity g
        LEFT JOIN AttachmentEntity a ON g.imageId = a.id
        WHERE g.id = :id
    """,
    )
    fun findWithAttachmentById(id: Long): GuideImageWithAttachment?
}
