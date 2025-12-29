package com.mediquitous.productpoc.service.index.chain.product

import com.mediquitous.productpoc.model.document.CategoryDocument
import com.mediquitous.productpoc.repository.jpa.product.ProductCategorySetJpaRepository
import com.mediquitous.productpoc.service.index.chain.BaseIndexChain
import com.mediquitous.productpoc.service.index.chain.IndexContext
import com.mediquitous.productpoc.service.index.chain.ProductDocumentBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

private const val ORIGINAL_CATEGORY_NAME = "original"
private const val SHOES_CATEGORY_NAME = "shoes"

/**
 * 카테고리 정보 체인
 *
 * Go 서버의 category_chain.go 로직을 Kotlin으로 변환
 */
@Component
class CategoryChain(
    private val productCategorySetRepository: ProductCategorySetJpaRepository,
    // TODO: CategoryService 주입 (부모 카테고리 조회용)
) : BaseIndexChain<ProductDocumentBuilder>() {
    override fun handle(context: IndexContext<ProductDocumentBuilder>): ProductDocumentBuilder? {
        val builder = context.document
        val productId = builder.id

        logger.debug { "CategoryChain 처리 시작: productId=$productId" }

        // 1. 상품-카테고리 매핑 조회
        val productCategories = productCategorySetRepository.findByProductId(productId)
        if (productCategories.isEmpty()) {
            logger.debug { "카테고리가 없음: productId=$productId" }
            return next(context)
        }

        // 2. 카테고리 ID 목록 추출
        val categoryIds = productCategories.mapNotNull { it.categoryId }

        // 3. 카테고리 상세 및 부모 카테고리 조회
        // TODO: CategoryService를 통해 부모 카테고리까지 조회하여 계층 구조 생성
        // 현재는 직접 매핑된 카테고리만 추가

        val categories =
            productCategories.mapNotNull { pcs ->
                // TODO: 카테고리 엔티티에서 상세 정보 조회
                CategoryDocument(
                    id = pcs.categoryId ?: return@mapNotNull null,
                    parentId = null, // TODO: 부모 카테고리 조회
                    name = null, // TODO: 카테고리명 조회
                    displayName = null,
                    slug = null,
                    isVisible = true,
                    isLeaf = false,
                )
            }

        builder.categories.addAll(categories)

        // 4. 특수 카테고리 플래그 설정
        // TODO: 카테고리명으로 original/shoes 여부 판단
        categories.forEach { category ->
            val name = category.name?.lowercase()
            when (name) {
                ORIGINAL_CATEGORY_NAME -> builder.isOriginal = true
                SHOES_CATEGORY_NAME -> builder.hasShoeCategory = true
            }
        }

        logger.debug { "CategoryChain 처리 완료: productId=$productId, categoryCount=${categories.size}" }
        return next(context)
    }
}
