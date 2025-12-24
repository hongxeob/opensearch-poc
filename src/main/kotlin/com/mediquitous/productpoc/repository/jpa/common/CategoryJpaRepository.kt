package com.mediquitous.productpoc.repository.jpa.common

import com.mediquitous.productpoc.repository.jpa.common.entity.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Suppress("ktlint:standard:kdoc")
/**
 * 카테고리 JPA Repository
 *
 * zelda-product의 db/queries/category.sql 기반
 * - GetCategories: 모든 카테고리 조회
 */
@Repository
interface CategoryJpaRepository : JpaRepository<CategoryEntity, Long> {
    /**
     * 모든 카테고리 조회
     *
     * SQL: GetCategories
     * Spring Data JPA의 findAll()이 동일한 기능을 제공
     */
}
