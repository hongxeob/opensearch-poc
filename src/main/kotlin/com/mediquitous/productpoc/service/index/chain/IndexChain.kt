package com.mediquitous.productpoc.service.index.chain

/**
 * 인덱싱 체인 인터페이스
 *
 * Go 서버의 chains/chain.go 구조를 Kotlin으로 변환
 * Chain of Responsibility 패턴으로 상품 문서를 단계별로 조립
 *
 * @param T 처리할 문서 타입
 */
interface IndexChain<T> {
    /**
     * 다음 체인 설정
     *
     * @param next 다음 체인
     * @return 설정된 다음 체인 (fluent API 지원)
     */
    fun setNext(next: IndexChain<T>): IndexChain<T>

    /**
     * 문서 처리
     *
     * @param context 처리 컨텍스트
     * @return 처리된 문서 (null이면 삭제 대상)
     */
    fun handle(context: IndexContext<T>): T?
}

/**
 * 인덱싱 컨텍스트
 *
 * 체인 간 데이터 전달을 위한 컨텍스트
 */
data class IndexContext<T>(
    val document: T,
    val metadata: MutableMap<String, Any> = mutableMapOf(),
)

/**
 * 체인 기본 구현체
 *
 * Go 서버의 chains.BaseChain 구조를 Kotlin으로 변환
 */
abstract class BaseIndexChain<T> : IndexChain<T> {
    protected var next: IndexChain<T>? = null

    override fun setNext(next: IndexChain<T>): IndexChain<T> {
        this.next = next
        return next
    }

    /**
     * 다음 체인 호출
     */
    protected fun next(context: IndexContext<T>): T? = next?.handle(context) ?: context.document
}
