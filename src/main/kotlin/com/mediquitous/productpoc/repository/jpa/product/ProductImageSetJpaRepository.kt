package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.ProductImageSetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 상품 이미지 세트 JPA Repository
 */
@Repository
interface ProductImageSetJpaRepository : JpaRepository<ProductImageSetEntity, Long>
