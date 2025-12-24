package com.mediquitous.productpoc.repository.jpa

import com.mediquitous.productpoc.repository.jpa.entity.ProductGuideImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

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
}
