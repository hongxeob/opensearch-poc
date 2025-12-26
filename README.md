# Product Search Service (POC)

Go 기반의 `zelda-product` 서비스를 Spring Boot + Kotlin으로 마이그레이션한 프로젝트입니다.

## 🎯 프로젝트 개요

| 항목 | 설명 |
|------|------|
| **목적** | 패션 이커머스 상품 검색 서비스 |
| **검색 엔진** | OpenSearch 2.x |
| **DB** | PostgreSQL (읽기 전용) |
| **실시간 동기화** | Kafka CDC (Debezium) |
| **캐싱** | Redis (버퍼링) |

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
- **Controller**: HTTP 요청/응답 처리 (`@RestController`)
- **Service**: 비즈니스 로직 - 검색, 인덱싱, 마이그레이션 (`@Service`)
- **Repository**: 데이터 액세스 - JPA, OpenSearch (`@Repository`)

## 🚀 기술 스택

| 영역 | 기술 |
|------|------|
| 언어 | Kotlin 1.9.x |
| 프레임워크 | Spring Boot 3.x |
| 검색 엔진 | OpenSearch 2.x (opensearch-java client) |
| 데이터베이스 | PostgreSQL 16 |
| 메시징 | Apache Kafka |
| 캐싱 | Redis |
| ORM | Spring Data JPA |
| 문서화 | SpringDoc OpenAPI |
| 빌드 | Gradle Kotlin DSL |

## 📦 프로젝트 구조

```
src/main/kotlin/com/mediquitous/productpoc/
├── config/                         # 설정
│   ├── KafkaConfig.kt             # Kafka 설정
│   ├── OpenSearchConfig.kt        # OpenSearch 클라이언트 설정
│   └── RedisConfig.kt             # Redis 설정
│
├── controller/                     # HTTP 컨트롤러
│   ├── ProductQueryController.kt  # 상품 조회 API
│   ├── ProductCommandController.kt # 상품 인덱싱/마이그레이션 API
│   ├── SellerCommandController.kt # 셀러 인덱싱 API
│   └── system/
│       └── GlobalExceptionHandler.kt
│
├── service/                        # 비즈니스 로직
│   ├── ProductSearchService.kt    # 상품 검색 인터페이스
│   ├── ProductSearchServiceImpl.kt # 상품 검색 구현체
│   ├── ProductIndexService.kt     # 상품 인덱싱 인터페이스
│   ├── ProductIndexServiceImpl.kt # 상품 인덱싱 구현체
│   ├── ProductMigrationService.kt # 마이그레이션 인터페이스
│   ├── ProductMigrationServiceImpl.kt
│   ├── SellerIndexService.kt
│   ├── SellerIndexServiceImpl.kt
│   ├── SellerMigrationService.kt
│   ├── SellerMigrationServiceImpl.kt
│   └── event/                     # 이벤트 처리
│       ├── ProductEventBuffer.kt  # 이벤트 버퍼 인터페이스
│       ├── ProductEventBufferImpl.kt
│       ├── handler/               # Kafka 컨슈머 핸들러
│       ├── producer/              # Kafka 프로듀서
│       └── topic/                 # 토픽 정의
│
├── repository/
│   ├── opensearch/                # OpenSearch 리포지토리
│   │   ├── OpenSearchRepository.kt
│   │   ├── OpenSearchRepositoryImpl.kt
│   │   └── query/
│   │       └── ProductSearchQueryBuilder.kt  # 쿼리 빌더
│   │
│   └── jpa/                       # JPA 리포지토리
│       ├── product/               # 상품 관련
│       │   ├── entity/
│       │   ├── ProductJpaRepository.kt
│       │   ├── ProductVariantJpaRepository.kt
│       │   ├── OptionJpaRepository.kt
│       │   ├── StockJpaRepository.kt
│       │   └── ...
│       ├── seller/                # 셀러 관련
│       ├── benefit/               # 혜택/쿠폰 관련
│       ├── displaygroup/          # 기획전 관련
│       ├── ranking/               # 랭킹 관련
│       ├── customer/              # 고객 관련
│       ├── order/                 # 주문 관련
│       └── common/                # 공통 (카테고리, 브랜드 등)
│
└── model/
    └── dto/                       # 데이터 전송 객체
        ├── ProductDto.kt
        └── RecentlyViewedProductDto.kt
```

## 🔍 상품 검색 서비스

### ProductSearchService - 지원 검색 유형

| 메서드 | 설명 | Go 원본 |
|--------|------|---------|
| `getProductById()` | 상품 단건 조회 | `by_id_service.go` |
| `getProductsByIds()` | ID 목록 조회 | `by_ids_service.go` |
| `getProductsByDisplayGroup()` | 기획전별 상품 | `by_display_group_id_service.go` |
| `getProductsBySeller()` | 셀러별 상품 | `by_seller_slug_service.go` |
| `getProductsByCategory()` | 카테고리별 상품 | `by_category_slug_service.go` |
| `getProductsByCategoryAndSeller()` | 카테고리+셀러 | `by_category_slug_seller_slug_service.go` |
| `searchByKeyword()` | 키워드 검색 | `by_keyword_search_service.go` |
| `searchByKeywordWithFilters()` | 키워드+필터 검색 | `by_keyword_search_with_seller_type_category_service.go` |
| `getProductsByHomeTab()` | 홈탭별 상품 | `by_home_tab_type_service.go` |
| `getNewestProducts()` | 신상품 | `by_newest_service.go` |
| `getRecommendProducts()` | 추천 상품 | `recommend_by_codes_service.go` |
| `getProductsByCategoryId()` | 카테고리 ID별 | `by_category_id_service.go` |
| `getProductsByRetailStore()` | 리테일 스토어별 | `by_retail_store_name_service.go` |
| `getProductsByBestRanking()` | 베스트 랭킹 | `by_best_ranking_service.go` (DB 연동 필요) |
| `getLikedProducts()` | 좋아요 상품 | `by_customer_id_liked_service.go` (DB 연동 필요) |
| `getRecentlyViewedProducts()` | 최근 본 상품 | `by_customer_id_recently_viewed_service.go` (DB 연동 필요) |

### ProductSearchQueryBuilder - 쿼리 빌더

| 메서드 | 설명 |
|--------|------|
| `buildKeywordSearchQuery()` | 키워드 검색 쿼리 (multi-match, ngram) |
| `buildProductIdsQuery()` | 상품 ID 목록 쿼리 |
| `buildCategorySlugQuery()` | 카테고리 슬러그 쿼리 |
| `buildSellerSlugQuery()` | 셀러 슬러그 쿼리 |
| `buildDisplayGroupQuery()` | 기획전 쿼리 |
| `buildCategoryAndSellerSlugQuery()` | 카테고리+셀러 쿼리 |
| `buildHomeTabQuery()` | 홈탭 쿼리 (brand, director, beauty) |
| `buildNewestQuery()` | 신상품 쿼리 |
| `buildRecommendByCodesQuery()` | 추천 상품 코드 쿼리 |
| `buildCategoryIdQuery()` | 카테고리 ID 쿼리 |
| `buildRetailStoreQuery()` | 리테일 스토어 쿼리 |
| `buildKeywordWithFiltersQuery()` | 키워드+필터 쿼리 |
| `buildProductIdsBulkQuery()` | 벌크 검색 쿼리 |

### 정렬 옵션

| 정렬 키 | 설명 |
|---------|------|
| `released` / `-released` | 출시일 오름차순/내림차순 |
| `in_stock` | 재고 우선 |
| `productbestorder` | 인기순 (order_count, like_count, cart_count) |
| `sales_amount` / `-sales_amount` | 판매액 |
| `displaygroupproduct__seq` | 기획전 순서 |

## 🌐 API 엔드포인트

### 상품 조회 API (ProductQueryController)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/products/search` | 키워드 검색 |
| GET | `/api/v1/products/{id}` | 상품 단건 조회 |
| GET | `/api/v1/products` | 상품 ID 목록 조회 |
| GET | `/api/v1/products/category/{slug}` | 카테고리별 조회 |
| GET | `/api/v1/products/seller/{slug}` | 셀러별 조회 |
| GET | `/api/v1/products/display-group/{id}` | 기획전별 조회 |
| GET | `/api/v1/products/home-tab/{tab}` | 홈탭별 조회 |
| GET | `/api/v1/products/newest` | 신상품 조회 |
| GET | `/api/v1/products/best-ranking` | 베스트 랭킹 |
| GET | `/api/v1/products/recommend` | 추천 상품 |

### 상품 인덱싱/마이그레이션 API (ProductCommandController)

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/products/migrate` | 전체 상품 마이그레이션 |
| POST | `/api/v1/products/migrate-by-ids` | 특정 상품 마이그레이션 |
| POST | `/api/v1/products/index` | 상품 인덱싱 |

## 🔧 설정

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

## 🔄 데이터 흐름

### 1. 검색 쿼리
```
HTTP Request → Controller → ProductSearchService → OpenSearchRepository → OpenSearch
```

### 2. 데이터 동기화 (CDC)
```
PostgreSQL → Debezium → Kafka → EventHandler → ProductEventBuffer → OpenSearch
```

### 3. 마이그레이션
```
ProductMigrationService → JPA Repository → ProductIndexService → OpenSearch
```

## 🏃 실행 방법

### 1. 빌드

```bash
./gradlew clean build
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. API 문서

Swagger UI: http://localhost:8080/swagger-ui.html

## 🔀 Go → Kotlin 변환 규칙

| Go | Kotlin + Spring |
|---|---|
| `func NewXxxService(...deps) XxxService` | `@Service class XxxServiceImpl(deps) : XxxService` |
| `context.Context` | 생략 (Spring 관리) |
| `uber/fx DI` | Spring 생성자 주입 |
| `opensearch-go/v4` | `opensearch-java 2.x` |
| `text/template` (쿼리) | `SearchRequest.Builder` (Type-safe) |
| `kafka.MessageHandler` | `@KafkaListener` |
| `sqlc` | Spring Data JPA `@Entity` + `@Repository` |
| `error` 반환 | Exception throw + `@ControllerAdvice` |
| `zap.Logger` | `KotlinLogging.logger` |

## 📋 TODO

- [x] ProductSearchService 구현
- [x] ProductSearchQueryBuilder 구현
- [x] OpenSearchRepository 구현
- [x] JPA 엔티티 및 리포지토리 구현
- [x] Kafka 이벤트 버퍼 구현
- [ ] 베스트 랭킹 DB 연동 (PostgreSQL 랭킹 스펙 조회)
- [ ] 좋아요/최근 본 상품 DB 연동
- [ ] 테스트 코드 작성 (Kotest)
- [ ] Docker Compose 설정
- [ ] CI/CD 파이프라인 구축

## 📚 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [OpenSearch Java Client](https://opensearch.org/docs/latest/clients/java/)
- [Spring Kafka 문서](https://spring.io/projects/spring-kafka)
- [Kotest 문서](https://kotest.io/)

## 👥 기여자

- 홍섭 (Backend Developer)

## 📝 라이센스

Proprietary - Mediquitous Inc.
