package com.mediquitous.productpoc.model.vo

import com.mediquitous.productpoc.repository.jpa.common.entity.CategoryEntity

/**
 * 카테고리 도메인 모델
 *
 * Go 서버의 internal/service/category/category.go 변환
 *
 * 트리 구조를 가지며, 부모-자식 관계를 통해 카테고리 계층을 표현
 */
data class Category(
    val id: Long,
    val parentId: Long,
    val name: String,
    val displayName: String,
    val slug: String?,
    val isVisible: Boolean,
    var isLeaf: Boolean = true,
    var parent: Category? = null,
    var children: List<Category> = emptyList(),
) {
    /**
     * 자식 카테고리 설정
     *
     * 자식이 있으면 isLeaf = false, 없으면 isLeaf = true
     * 각 자식의 parent를 현재 카테고리로 설정
     */
    fun attachChildren(children: List<Category>) {
        if (children.isEmpty()) {
            this.isLeaf = true
            this.children = emptyList()
            return
        }
        this.children = children
        this.isLeaf = false
        children.forEach { child ->
            child.parent = this
        }
    }

    /**
     * 조상 카테고리 목록 조회
     *
     * @param includeSelf 자기 자신 포함 여부
     * @return 조상 카테고리 목록 (순서 보장 없음)
     */
    fun getAncestors(includeSelf: Boolean): List<Category> {
        val found = mutableMapOf<Long, Category>()
        var current: Category? = this

        if (includeSelf) {
            found[this.id] = this
        }

        while (current?.parent != null) {
            found[current.id] = current
            current = current.parent
        }

        return found.values.toList()
    }

    companion object {
        /**
         * Entity → Domain 변환
         */
        fun from(entity: CategoryEntity): Category =
            Category(
                id = entity.id ?: 0L,
                parentId = entity.parentId ?: 0L,
                name = entity.name ?: "",
                displayName = entity.displayName ?: "",
                slug = entity.slug,
                isVisible = entity.isVisible ?: false,
                isLeaf = true,
            )
    }
}
