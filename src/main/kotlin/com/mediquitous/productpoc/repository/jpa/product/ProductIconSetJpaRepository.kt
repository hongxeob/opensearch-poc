package com.mediquitous.productpoc.repository.jpa.product

import com.mediquitous.productpoc.repository.jpa.product.entity.ProductIconSetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 상품 아이콘 세트 JPA Repository
 */
@Repository
interface ProductIconSetJpaRepository : JpaRepository<ProductIconSetEntity, Long>
