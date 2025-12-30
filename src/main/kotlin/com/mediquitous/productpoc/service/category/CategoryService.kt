package com.mediquitous.productpoc.service.category

import com.mediquitous.productpoc.model.vo.Category

/**
 * 카테고리 서비스 인터페이스
 *
 * Go 서버의 internal/service/category/service.go 변환
 */
interface CategoryService {
    /**
     * 카테고리 ID 목록으로 해당 카테고리와 모든 조상 카테고리 조회
     *
     * @param categoryIds 카테고리 ID 목록
     * @return 카테고리 목록 (자기 자신 + 모든 조상)
     */
    fun getParentsByIds(categoryIds: List<Long>): List<Category>

    /**
     * 카테고리 캐시 갱신
     *
     * Redis 캐시에 모든 카테고리를 다시 로드
     */
    fun loadCache()
}
