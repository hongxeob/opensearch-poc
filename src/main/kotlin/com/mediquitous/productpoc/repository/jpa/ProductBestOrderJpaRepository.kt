package com.mediquitous.productpoc.repository.jpa

import com.mediquitous.productpoc.repository.jpa.entity.ProductBestOrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 상품 베스트 정렬 JPA Repository
 *
 * zelda-product의 db/queries/productbestorder.sql 기반
 * - GetProductBestOrderByProductID: 상품 ID로 베스트 정렬 정보 조회
 */
@Repository
interface ProductBestOrderJpaRepository : JpaRepository<ProductBestOrderEntity, Long> {
    /**
     * 상품 ID로 베스트 정렬 정보 조회
     *
     * SQL: GetProductBestOrderByProductID
     * Spring Data JPA의 메서드 명명 규칙 사용
     */
    fun findByProductId(productId: Long): ProductBestOrderEntity?
}
