package com.mediquitous.productpoc.service.event.debezium.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mediquitous.productpoc.repository.jpa.product.ProductCategorySetJpaRepository
import com.mediquitous.productpoc.service.event.ProductEventBuffer
import com.mediquitous.productpoc.service.event.debezium.CategoryPayload
import com.mediquitous.productpoc.service.event.debezium.ChangeEvent
import com.mediquitous.productpoc.service.event.debezium.DebeziumTopics
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

private const val PAGE_SIZE = 1000

/**
 * shopping_category 테이블 CDC 핸들러
 *
 * Go 서버의 internal/event/handler/debezium/category_handler.go 변환
 *
 * Category 변경 시:
 * 1. 카테고리 캐시 갱신 (TODO: CategoryService 구현 시 추가)
 * 2. 해당 카테고리에 속한 모든 상품 ID를 버퍼에 추가 (커서 페이징)
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class CategoryDebeziumHandler(
    private val objectMapper: ObjectMapper,
    private val productEventBuffer: ProductEventBuffer,
    private val productCategorySetJpaRepository: ProductCategorySetJpaRepository,
) {
    @KafkaListener(
        topics = [DebeziumTopics.CATEGORY],
        groupId = "\${kafka.group-name}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handle(
        @Payload payload: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment,
    ) {
        logger.debug { "Category CDC 수신: partition=$partition, offset=$offset" }

        try {
            val event =
                objectMapper.readValue(
                    payload,
                    object : TypeReference<ChangeEvent<CategoryPayload>>() {},
                )
            val record = event.record()

            // TODO: CategoryService.loadCache() 호출 (카테고리 캐시 갱신)
            // categoryService.loadCache()

            // 해당 카테고리에 속한 모든 상품 ID를 버퍼에 추가 (커서 페이징)
            var afterProductId = 0L
            var totalCount = 0

            while (true) {
                val productIds =
                    productCategorySetJpaRepository.findProductIdsByCategoryId(
                        categoryId = record.id,
                        afterProductId = afterProductId,
                        limit = PAGE_SIZE,
                    )

                if (productIds.isEmpty()) break

                productEventBuffer.add(productIds)
                totalCount += productIds.size

                logger.debug { "Category 상품 버퍼 추가: categoryId=${record.id}, batch=${productIds.size}" }

                if (productIds.size < PAGE_SIZE) break
                afterProductId = productIds.last()
            }

            acknowledgment.acknowledge()
            logger.info { "Category CDC 처리 완료: categoryId=${record.id}, totalProducts=$totalCount" }
        } catch (e: Exception) {
            logger.error(e) { "Category CDC 처리 실패" }
            throw e
        }
    }
}
