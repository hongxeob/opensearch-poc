package com.mediquitous.productpoc.service.event.debezium

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Debezium CDC 이벤트 공통 모델
 *
 * Go 서버의 internal/event/handler/debezium/change_event.go 변환
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ChangeEvent<T>(
    val payload: Payload<T>,
) {
    /**
     * 현재 레코드 반환
     * - CREATE/UPDATE: after 값
     * - DELETE: before 값
     */
    fun record(): T =
        when (payload.op) {
            Operation.CREATE, Operation.UPDATE -> {
                payload.after
                    ?: throw IllegalStateException("after is null for operation: ${payload.op}")
            }

            Operation.DELETE -> {
                payload.before
                    ?: throw IllegalStateException("before is null for delete operation")
            }

            else -> {
                throw IllegalStateException("Unknown operation type: ${payload.op}")
            }
        }

    fun isDelete(): Boolean = payload.op == Operation.DELETE
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Payload<T>(
    /** 변경 전 레코드 (DELETE 시 사용) */
    val before: T?,
    /** 변경 후 레코드 (CREATE/UPDATE 시 사용) */
    val after: T?,
    /** 작업 유형: c(create), u(update), d(delete), r(read/snapshot) */
    val op: String,
    /** 변경 소스 메타데이터 */
    val source: SourceInfo?,
    /** 타임스탬프 (밀리초) */
    @JsonProperty("ts_ms")
    val tsMs: Long?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SourceInfo(
    /** Debezium 커넥터 버전 */
    val version: String?,
    /** 소스 DB 타입 (e.g., postgresql) */
    val connector: String?,
    /** 커넥터 이름 */
    val name: String?,
    /** 타임스탬프 (밀리초) */
    @JsonProperty("ts_ms")
    val tsMs: Long?,
    /** 스냅샷 여부 */
    val snapshot: String?,
    /** 데이터베이스 이름 */
    val db: String?,
    /** 스키마 이름 */
    val schema: String?,
    /** 테이블 이름 */
    val table: String?,
    /** 트랜잭션 ID */
    val txId: Long?,
    /** Log Sequence Number */
    val lsn: Long?,
)

/**
 * Debezium 작업 유형 상수
 */
object Operation {
    const val CREATE = "c"
    const val READ = "r"
    const val UPDATE = "u"
    const val DELETE = "d"
}
