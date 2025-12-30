# Product Search Service (POC)

Go 기반의 `zelda-product` 서비스를 Spring Boot + Kotlin으로 마이그레이션한 프로젝트입니다.

## 🎯 프로젝트 개요

| 항목 | 설명                   |
|------|----------------------|
| **목적** | 패션 이커머스 상품 검색 서비스    |
| **검색 엔진** | OpenSearch 2.x       |
| **DB** | PostgreSQL (읽기 전용)   |
| **실시간 동기화** | Kafka CDC (Debezium) |
| **캐싱** | Redis (버퍼링)          |

## 🏗️ 아키텍처

```
┌─────────────────┐
│   Controller    │  HTTP 요청 처리
└────────┬────────┘
         │
┌────────▼────────┐
│    Service      │  비즈니스 로직 + DTO 변환
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌──▼──────┐
│  JPA  │ │OpenSearch│  데이터 액세스 (Document 반환)
└───────┘ └─────────┘
```

### 3-Layer Architecture
- **Controller**: HTTP 요청/응답 처리 (`@RestController`)
- **Service**: 비즈니스 로직, Document → DTO 변환, 할인/혜택 적용 (`@Service`)
- **Repository**: 데이터 액세스 - JPA Entity, OpenSearch Document 반환 (`@Repository`)

### 레이어 간 데이터 흐름

```
OpenSearch ──→ ProductDocument ──→ ProductConvertService ──→ SimpleProduct/Product ──→ Controller
              (Repository 반환)        (Service 변환)           (DTO 반환)
```

**핵심 원칙**: Repository는 Document/Entity를 반환하고, Service에서 DTO로 변환

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
│   ├── search/                    # 검색 서비스
│   │   ├── ProductSearchService.kt
│   │   └── ProductSearchServiceImpl.kt
│   │
│   ├── product/                   # 상품 변환 서비스
│   │   ├── ProductConvertService.kt      # Document → DTO 변환 인터페이스
│   │   └── ProductConvertServiceImpl.kt  # 변환 + 할인/혜택/쿠폰 적용
│   │
│   ├── index/                     # 인덱싱 서비스
│   │   ├── ProductIndexService.kt
│   │   ├── ProductIndexServiceImpl.kt
│   │   └── chain/                 # Chain of Responsibility 패턴
│   │       ├── ProductDocumentBuilder.kt
│   │       └── ...Chain.kt
│   │
│   └── event/                     # 이벤트 처리
│       ├── ProductEventBuffer.kt
│       └── handler/
│
├── repository/
│   ├── opensearch/                # OpenSearch 리포지토리
│   │   ├── OpenSearchRepository.kt       # SearchResult<ProductDocument> 반환
│   │   ├── OpenSearchRepositoryImpl.kt   # Map → ProductDocument 변환
│   │   └── query/
│   │       └── ProductSearchQueryBuilder.kt
│   │
│   └── jpa/                       # JPA 리포지토리
│       ├── product/
│       ├── seller/
│       ├── benefit/
│       ├── displaygroup/
│       ├── ranking/
│       └── customer/
│
└── model/
    ├── document/                  # OpenSearch 문서 모델
    │   ├── ProductDocument.kt     # 상품 문서
    │   ├── SellerDocument.kt      # 셀러 문서
    │   ├── OptionDocument.kt      # 옵션 문서
    │   ├── VariantDocument.kt     # 품목 문서
    │   └── ...Document.kt
    │
    ├── dto/                       # 클라이언트 응답 DTO
    │   ├── Product.kt             # 상품 상세 DTO
    │   ├── SimpleProduct.kt       # 상품 목록 DTO (Go simple_product.go)
    │   ├── ProductOption.kt       # 옵션 DTO (Go option.go)
    │   ├── ProductVariant.kt      # 품목 DTO (Go variant.go)
    │   ├── Seller.kt              # 셀러 DTO
    │   ├── Benefit.kt             # 혜택 DTO
    │   ├── Coupon.kt              # 쿠폰 DTO
    │   └── ProductDto.kt          # 페이지네이션 응답
    │
    └── vo/                        # Value Objects
        ├── BestRankingPath.kt
        ├── RankedProduct.kt
        └── LikedProduct.kt
```

## 🔄 DTO 구조 (Go 서버와 동기화)

### ProductOption (option.go)
```kotlin
data class ProductOption(
    val id: Long,
    val name: String,           // 옵션 종류 (Color, Size)
    val value: String,          // 옵션 값 (black, M)
    val hexcode: String?,       // 색상 헥사코드
    val searchName: Any?,       // 검색용 색상명
    val model: Boolean,         // 모델 착용 옵션 여부
)
```

### ProductVariant (variant.go)
```kotlin
data class ProductVariant(
    val id: Long,
    val express: Boolean,                    // 빠른배송 여부
    val availableStockQuantities: Int,       // 가용 재고
    val price: Double,
    val discountPrice: Double,
    val soldOut: Boolean,
    val optionSet: List<ProductOption>,      // 옵션 목록
    val optionValues: String?,               // "Black / M" 형태
    val options: Map<String, Any>?,          // 옵션 맵
    // ... 기타 필드
)
```

### SimpleProduct (simple_product.go)
```kotlin
data class SimpleProduct(
    val id: Long,
    val code: String?,
    val name: String?,
    val price: Double,
    val discountPrice: Double,
    val discountRate: Double,
    val image: Attachment?,
    val optionSet: List<ProductOption>,
    val leafCategories: List<CategoryDto>,
    val seller: String,
    val sellerSlug: String?,
    val benefitEnd: OffsetDateTime?,         // 혜택 종료 시간
    val shippingFeeBenefit: Benefit?,        // 배송비 혜택
    val express: Boolean,
    val iconSet: List<String>,
    val reviewCount: Int,
    val reviewAverage: Double,
    val totalLikeCount: Int,
    // ... 기타 필드
)
```

## 🔍 ProductConvertService - 변환 서비스

### 역할
- **Document → DTO 변환**: OpenSearch Document를 클라이언트 응답용 DTO로 변환
- **비즈니스 로직 적용**: 할인, 혜택, 쿠폰 계산

### 주요 메서드

| 메서드 | 설명 |
|--------|------|
| `convertToProductDto(builders)` | ProductDocumentBuilder → Product (상세) |
| `convertToSimpleProductDto(builders)` | ProductDocumentBuilder → SimpleProduct (목록) |
| `convertDocumentsToSimpleProducts(documents)` | ProductDocument → SimpleProduct (검색 결과) |

### 변환 파이프라인
```
ProductDocument
    ↓
ProductDocumentBuilder (documentToBuilder)
    ↓
Product (builderToProduct)
    ↓ applyDisplayGroup()  - 활성 기획전 필터링
    ↓ applyBenefit()       - 혜택 적용 (고정/비율 할인)
    ↓ applyCoupon()        - 쿠폰 적용
    ↓
SimpleProduct (toSimple)
```

## 🔍 상품 검색 서비스

### ProductSearchService - 지원 검색 유형

| 메서드 | 설명            | Go 원본 |
|--------|---------------|---------|
| `getProductById()` | 상품 단건 조회      | `by_id_service.go` |
| `getProductsByIds()` | ID 목록 조회      | `by_ids_service.go` |
| `getProductsByDisplayGroup()` | 기획전별 상품       | `by_display_group_id_service.go` |
| `getProductsBySeller()` | 셀러별 상품        | `by_seller_slug_service.go` |
| `getProductsByCategory()` | 카테고리별 상품      | `by_category_slug_service.go` |
| `getProductsByCategoryAndSeller()` | 카테고리+셀러       | `by_category_slug_seller_slug_service.go` |
| `searchByKeyword()` | 키워드 검색        | `by_keyword_search_service.go` |
| `searchByKeywordWithFilters()` | 키워드+필터 검색     | `by_keyword_search_with_seller_type_category_service.go` |
| `getProductsByHomeTab()` | 홈탭별 상품        | `by_home_tab_type_service.go` |
| `getNewestProducts()` | 신상품           | `by_newest_service.go` |
| `getRecommendProducts()` | 추천 상품         | `recommend_by_codes_service.go` |
| `getProductsByCategoryId()` | 카테고리 ID별      | `by_category_id_service.go` |
| `getProductsByRetailStore()` | 리테일 스토어별      | `by_retail_store_name_service.go` |
| `getProductsByBestRanking()` | 베스트 랭킹        | `by_best_ranking_service.go` |
| `getLikedProducts()` | 좋아요 상품        | `by_customer_id_liked_service.go` |
| `getRecentlyViewedProducts()` | 최근 본 상품       | `by_customer_id_recently_viewed_service.go` |

### OpenSearchRepository - 검색 결과

```kotlin
data class SearchResult(
    val totalHits: Long,
    val documents: List<ProductDocument>,  // Document 반환 (DTO 아님)
    val nextCursor: String?,
)
```

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
HTTP Request 
    → Controller 
    → ProductSearchService 
    → OpenSearchRepository (ProductDocument 반환)
    → ProductConvertService (SimpleProduct 변환)
    → Controller (JSON 응답)
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
| `dto.Option` | `ProductOption` |
| `dto.Variant` | `ProductVariant` |
| `dto.SimpleProduct` | `SimpleProduct` |
| `dao.Product` (Document) | `ProductDocument` |

### DTO 필드 매핑 규칙

| Go 필드 | Kotlin 필드 | 비고 |
|---------|-------------|------|
| `snake_case` JSON | `@JsonProperty("snake_case")` | Jackson 어노테이션 |
| `*string` (nullable) | `String?` | Kotlin nullable |
| `interface{}` | `Any?` | 동적 타입 |
| `[]Option` | `List<ProductOption>` | 불변 리스트 |
| `map[string]any` | `Map<String, Any>?` | nullable 맵 |

## 📋 TODO

- [x] ProductSearchService 구현
- [x] ProductSearchQueryBuilder 구현
- [x] OpenSearchRepository 구현 (Document 반환)
- [x] ProductConvertService 구현 (Document → DTO 변환)
- [x] JPA 엔티티 및 리포지토리 구현
- [x] Kafka 이벤트 버퍼 구현
- [x] DTO Go 서버와 동기화 (ProductOption, ProductVariant, SimpleProduct)
- [x] 베스트 랭킹 DB 연동
- [x] 좋아요/최근 본 상품 DB 연동
- [x] CDC 이벤트 구현
- [ ] 테스트 코드 작성 (Kotest)
- [ ] Docker Compose 설정
- [ ] CI/CD 파이프라인 구축

## 📚 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [OpenSearch Java Client](https://opensearch.org/docs/latest/clients/java/)
- [Spring Kafka 문서](https://spring.io/projects/spring-kafka)
- [Kotest 문서](https://kotest.io/)

## 📝 라이센스

Proprietary - Mediquitous Inc.
