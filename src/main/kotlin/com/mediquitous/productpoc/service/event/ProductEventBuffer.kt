package com.mediquitous.productpoc.service.event

/**
 * 상품 이벤트 버퍼
 *
 * Redis를 사용하여 상품 ID를 버퍼링하고, Kafka로 이벤트를 발행합니다.
 */
interface ProductEventBuffer {
    /**
     * 상품 ID를 버퍼에 추가
     *
     * Redis List에 상품 ID를 추가합니다 (RPUSH).
     *
     * @param productIds 추가할 상품 ID 목록
     */
    fun add(productIds: List<Long>)

    /**
     * 버퍼에서 상품 ID를 꺼내서 이벤트 발행
     *
     * Redis List에서 상품 ID를 꺼내고 (LPOP),
     * 중복 제거 후 Kafka로 product.updated 이벤트를 발행합니다.
     *
     * @param count 처리할 개수
     */
    fun flush(count: Int)
}
