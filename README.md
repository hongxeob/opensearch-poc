# Product Search Service (POC)

Go 기반의 `zelda-product` 서비스를 Spring Boot + Kotlin으로 마이그레이션한 프로젝트입니다.

## 🎯 프로젝트 개요

- **목적**: 패션 이커머스 상품 검색 서비스
- **검색 엔진**: OpenSearch 3.x
- **DB**: PostgreSQL (읽기 전용)
- **실시간 동기화**: Kafka CDC (Debezium)
- **캐싱**: Redis

## 🏗️ 아키텍처

```
┌─────────────────┐
│   Controller    │  HTTP 요청 처리
└────────┬────────┘
         │
┌────────▼────────┐
│    Service      │  비즈니스 로직
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌──▼──────┐
│  JPA  │ │OpenSearch│  데이터 액세스
└───────┘ └─────────┘
```

### 3-Layer Architecture
- **Controller**: HTTP 요청/응답 처리
- **Service**: 비즈니스 로직 (검색, 변환)
- **Repository**: 데이터 액세스 (JPA, OpenSearch)

## 🚀 기술 스택

| 영역 | 기술 |
|------|------|
| 언어 | Kotlin 1.9.25 |
| 프레임워크 | Spring Boot 3.5.9 |
| 검색 엔진 | OpenSearch 3.x |
| 데이터베이스 | PostgreSQL 16 |
| 메시징 | Apache Kafka |
| 캐싱 | Redis |
| ORM | Spring Data JPA |
| 문서화 | SpringDoc OpenAPI |
| 빌드 | Gradle Kotlin DSL |

## 📦 프로젝트 구조

```
src/main/kotlin/com/mediquitous/productpoc/
├── adapter/                    # 외부 어댑터
│   └── web/                   # HTTP 컨트롤러
│       ├── ProductController.kt
│       └── GlobalExceptionHandler.kt
├── application/               # 애플리케이션 서비스 (TODO)
│   ├── service/              # 비즈니스 로직
│   └── port/                 # 포트 인터페이스
├── domain/                    # 도메인 모델
│   ├── model/                # 도메인 엔티티
│   └── dto/                  # 데이터 전송 객체
└── infrastructure/           # 인프라 구현 (TODO)
    ├── config/               # 설정
    ├── opensearch/           # OpenSearch 클라이언트
    ├── jpa/                  # JPA 엔티티 & 리포지토리
    └── kafka/                # Kafka 컨슈머
```

## 🔧 설정

### application.yml

주요 설정 항목:
- PostgreSQL 연결 정보
- OpenSearch 호스트 및 인덱스
- Kafka 브로커 및 토픽
- Redis 연결 정보

### 환경 변수

```bash
# PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/zelda_product
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# OpenSearch
OPENSEARCH_HOSTS=localhost:9200
OPENSEARCH_USERNAME=admin
OPENSEARCH_PASSWORD=admin

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
```

## 🌐 API 엔드포인트

### 상품 검색

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/products/search` | 키워드 검색 |
| GET | `/api/v1/products/{id}` | 상품 단건 조회 |
| GET | `/api/v1/products` | 상품 ID 목록 조회 |
| GET | `/api/v1/products/category` | 카테고리별 조회 |
| GET | `/api/v1/products/seller` | 셀러별 조회 |
| GET | `/api/v1/products/newest` | 신상품 조회 |
| GET | `/api/v1/products/best-ranking` | 베스트 랭킹 |
| GET | `/api/v1/products/liked` | 좋아요한 상품 |
| GET | `/api/v1/products/recently-viewed` | 최근 본 상품 |

### 관리자 API

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/products/migrate` | 전체 상품 마이그레이션 |
| POST | `/api/v1/products/migrate-by-ids` | 특정 상품 마이그레이션 |

## 📖 API 문서

Swagger UI: http://localhost:8080/swagger-ui.html

## 🔄 데이터 흐름

### 1. 검색 쿼리
```
HTTP Request → Controller → Service → OpenSearch → Response
```

### 2. 데이터 동기화 (CDC)
```
PostgreSQL → Debezium → Kafka → Consumer → OpenSearch
```

### 3. 캐싱
```
Request → Redis Cache Check → (Miss) → OpenSearch → Cache Update
```

## 🏃 실행 방법

### 1. 의존성 설치

```bash
./gradlew clean build
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
java -jar build/libs/product-poc-0.0.1-SNAPSHOT.jar
```

### 3. Docker Compose로 인프라 실행

```bash
# TODO: docker-compose.yml 추가 예정
docker-compose up -d
```

## 🧪 테스트

```bash
./gradlew test
```

## 📋 TODO

- [ ] Service 레이어 구현
- [ ] OpenSearch 쿼리 빌더 구현
- [ ] JPA 엔티티 및 리포지토리 구현
- [ ] Kafka 컨슈머 구현 (CDC)
- [ ] Redis 캐싱 구현
- [ ] 테스트 코드 작성 (Kotest)
- [ ] Docker Compose 설정
- [ ] CI/CD 파이프라인 구축

## 🔀 Go vs Kotlin 변환 규칙

| Go | Kotlin + Spring |
|---|---|
| `func NewXxxService(...deps) XxxService` | `@Service class XxxService(deps)` |
| `context.Context` | Kotlin Coroutine (선택) |
| `uber/fx DI` | Spring 생성자 주입 |
| `opensearch-go/v4` | `opensearch-java 3.x` |
| `kafka.MessageHandler` | `@KafkaListener` |
| `sqlc` | JPA `@Entity` + `@Repository` |
| `error` 반환 | Exception throw |

## 📚 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [OpenSearch Java Client](https://opensearch.org/docs/latest/clients/java/)
- [Spring Kafka 문서](https://spring.io/projects/spring-kafka)
- [Kotest 문서](https://kotest.io/)

## 👥 기여자

- 홍섭 (Backend Developer)

## 📝 라이센스

Proprietary - Mediquitous Inc.
# opensearch-poc
