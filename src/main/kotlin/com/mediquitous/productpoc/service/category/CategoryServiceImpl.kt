package com.mediquitous.productpoc.service.category

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.model.vo.Category
import com.mediquitous.productpoc.repository.jpa.common.CategoryJpaRepository
import com.mediquitous.productpoc.repository.jpa.common.entity.CategoryEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

private const val CACHE_KEY = "cache:categories:map"

/**
 * 카테고리 서비스 구현체
 *
 * Go 서버의 internal/service/category/service.go 변환
 *
 * Redis 캐시를 사용하여 카테고리 맵을 캐싱하고,
 * 카테고리 ID로 해당 카테고리와 모든 조상 카테고리를 조회
 */
@Service
class CategoryServiceImpl(
    private val categoryJpaRepository: CategoryJpaRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) : CategoryService {
    override fun getParentsByIds(categoryIds: List<Long>): List<Category> {
        val categoryMap = getOrSetCache()

        return categoryIds
            .mapNotNull { categoryId -> categoryMap[categoryId] }
            .flatMap { category -> category.getAncestors(includeSelf = true) }
            .distinctBy { it.id }
    }

    override fun loadCache() {
        logger.info { "카테고리 캐시 갱신 시작" }

        val entities = categoryJpaRepository.findAll()
        val categoryMap = toMap(entities)

        setCacheValue(categoryMap)

        logger.info { "카테고리 캐시 갱신 완료: ${categoryMap.size}개" }
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    /**
     * 캐시에서 조회하거나, 없으면 DB에서 로드 후 캐시에 저장
     */
    private fun getOrSetCache(): Map<Long, Category> {
        val cached = getCacheValue()
        if (cached != null) {
            return cached
        }

        logger.debug { "카테고리 캐시 미스, DB에서 로드" }

        val entities = categoryJpaRepository.findAll()
        val categoryMap = toMap(entities)

        setCacheValue(categoryMap)

        return categoryMap
    }

    /**
     * Redis 캐시에서 카테고리 맵 조회
     */
    private fun getCacheValue(): Map<Long, Category>? {
        return try {
            val json = redisTemplate.opsForValue().get(CACHE_KEY) ?: return null
            val cacheData =
                objectMapper.readValue(
                    json,
                    object : TypeReference<List<CategoryCacheData>>() {},
                )
            rebuildCategoryMap(cacheData)
        } catch (e: Exception) {
            logger.warn(e) { "카테고리 캐시 조회 실패" }
            null
        }
    }

    /**
     * Redis 캐시에 카테고리 맵 저장
     *
     * 트리 구조는 JSON으로 직렬화할 수 없으므로 (순환 참조),
     * 평탄화된 리스트로 저장 후 조회 시 재구성
     */
    private fun setCacheValue(categoryMap: Map<Long, Category>) {
        try {
            val cacheData =
                categoryMap.values.map { category ->
                    CategoryCacheData(
                        id = category.id,
                        parentId = category.parentId,
                        name = category.name,
                        displayName = category.displayName,
                        slug = category.slug,
                        isVisible = category.isVisible,
                    )
                }
            val json = objectMapper.writeValueAsString(cacheData)
            redisTemplate.opsForValue().set(CACHE_KEY, json)
        } catch (e: Exception) {
            logger.error(e) { "카테고리 캐시 저장 실패" }
        }
    }

    /**
     * 캐시 데이터로부터 카테고리 맵 재구성 (트리 구조 복원)
     */
    private fun rebuildCategoryMap(cacheData: List<CategoryCacheData>): Map<Long, Category> {
        val itemsById = mutableMapOf<Long, Category>()
        val itemsByParentId = mutableMapOf<Long, MutableList<Category>>()

        cacheData.forEach { data ->
            val category =
                Category(
                    id = data.id,
                    parentId = data.parentId,
                    name = data.name,
                    displayName = data.displayName,
                    slug = data.slug,
                    isVisible = data.isVisible,
                )
            itemsById[category.id] = category
            itemsByParentId.getOrPut(category.parentId) { mutableListOf() }.add(category)
        }

        // 트리 구조 구성
        itemsById.values.forEach { category ->
            val children = itemsByParentId[category.id] ?: emptyList()
            category.attachChildren(children)
        }

        return itemsById
    }

    /**
     * Entity 목록 → 카테고리 맵 (트리 구조 구성)
     */
    private fun toMap(entities: List<CategoryEntity>): Map<Long, Category> {
        val itemsById = mutableMapOf<Long, Category>()
        val itemsByParentId = mutableMapOf<Long, MutableList<Category>>()

        entities.forEach { entity ->
            val category = Category.from(entity)
            itemsById[category.id] = category
            itemsByParentId.getOrPut(category.parentId) { mutableListOf() }.add(category)
        }

        // 트리 구조 구성
        itemsById.values.forEach { category ->
            val children = itemsByParentId[category.id] ?: emptyList()
            category.attachChildren(children)
        }

        return itemsById
    }
}

/**
 * Redis 캐시 저장용 데이터 클래스
 *
 * 순환 참조 없이 평탄화된 형태로 저장
 */
private data class CategoryCacheData(
    val id: Long,
    val parentId: Long,
    val name: String,
    val displayName: String,
    val slug: String?,
    val isVisible: Boolean,
)
