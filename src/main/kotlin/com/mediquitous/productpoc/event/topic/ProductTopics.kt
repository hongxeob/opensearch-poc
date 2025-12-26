package com.mediquitous.productpoc.event.topic

/**
 * Kafka 토픽 상수 정의
 *
 * Go 서버의 event/topic/topics.go 구조 변환
 */
object ProductTopics {
    const val PRODUCT_UPDATED = "nugu.product.updated"
    const val PRODUCT_DELETED = "nugu.product.deleted"
    const val SELLER_UPDATED = "nugu.seller.updated"
    const val SELLER_DELETED = "nugu.seller.deleted"
}

/**
 * 상품 업데이트 이벤트
 */
data class ProductUpdatedEvent(
    val id: Long,
) {
    fun key(): String = id.toString()
}

/**
 * 상품 삭제 이벤트
 */
data class ProductDeletedEvent(
    val id: Long,
) {
    fun key(): String = id.toString()
}

/**
 * 셀러 업데이트 이벤트
 */
data class SellerUpdatedEvent(
    val id: Long,
) {
    fun key(): String = id.toString()
}

/**
 * 셀러 삭제 이벤트
 */
data class SellerDeletedEvent(
    val id: Long,
) {
    fun key(): String = id.toString()
}
