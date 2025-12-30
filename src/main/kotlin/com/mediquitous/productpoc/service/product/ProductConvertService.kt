package com.mediquitous.productpoc.service.product

import com.mediquitous.productpoc.model.document.ProductDocument
import com.mediquitous.productpoc.model.dto.ProductDto
import com.mediquitous.productpoc.model.dto.SimpleProductDto
import com.mediquitous.productpoc.service.index.chain.ProductDocumentBuilder

/**
 * 상품 변환 서비스 인터페이스
 *
 * Go 서버의 internal/service/product/convert_service.go 변환
 *
 * OpenSearch Document → Product DTO → SimpleProduct 변환 및
 * 비즈니스 로직(할인, 혜택, 쿠폰) 적용 담당
 */
interface ProductConvertService {
    /**
     * ProductDocumentBuilder 목록을 Product DTO로 변환
     *
     * 다음 비즈니스 로직을 순차적으로 적용:
     * 1. 기본 정보 매핑
     * 2. 활성 기획전 필터링
     * 3. 혜택 적용
     * 4. 쿠폰 적용
     *
     * @param builders OpenSearch에서 조회한 상품 빌더 목록
     * @return 비즈니스 로직이 적용된 Product DTO 목록
     */
    fun convertToProductDto(builders: List<ProductDocumentBuilder>): List<ProductDto>

    /**
     * ProductDocumentBuilder 목록을 SimpleProduct DTO로 변환
     *
     * Product로 변환 후 Simple()을 호출하여 간소화된 응답 생성
     *
     * @param builders OpenSearch에서 조회한 상품 빌더 목록
     * @return 클라이언트 응답용 SimpleProduct 목록
     */
    fun convertToSimpleProductDto(builders: List<ProductDocumentBuilder>): List<SimpleProductDto>

    /**
     * ProductDocument 목록을 SimpleProduct DTO로 변환
     *
     * OpenSearch에서 직접 조회한 Document를 클라이언트 응답용으로 변환
     *
     * @param documents OpenSearch에서 조회한 상품 문서 목록
     * @return 클라이언트 응답용 SimpleProduct 목록
     */
    fun convertDocumentsToSimpleProducts(documents: List<ProductDocument>): List<SimpleProductDto>
}
