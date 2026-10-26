package com.ragback.product.service;

import com.ragback.product.document.ProductDocument;
import com.ragback.product.entity.Product;
import com.ragback.product.repository.ProductDocumentRepository;
import com.ragback.product.repository.ProductRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDocumentRepository productDocumentRepository;
    @Autowired
    private EmbeddingModel embeddingModel;

    // 1) PostgreSQL에 상품 저장 → 2) 키워드 임베딩 → 3) Elasticsearch에 벡터 저장
    // 두 저장소는 하나의 트랜잭션으로 묶이지 않는다. 3)이 실패하면 "DB에는 있지만 검색되지 않는 상품"이 생긴다.
    public Product create(String name, List<String> keywords) {
        Product product = new Product();
        product.setName(name);
        keywords.forEach(product::addKeyword);
        productRepository.save(product);

        saveDocument(product.getId(), keywords);
        return product;
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findAll() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false).toList();
    }

    public Optional<ProductDocument> findDocumentById(Long id) {
        return productDocumentRepository.findById(id);
    }

    @Transactional
    public Product update(Long id, String name, List<String> keywords) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        product.setName(name);
        product.getKeywords().clear(); // orphanRemoval = true : 컬렉션에서 빠진 키워드는 DELETE 된다
        keywords.forEach(product::addKeyword);

        saveDocument(id, keywords);
        return product;
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
        productDocumentRepository.deleteById(id);
    }

    // Spring 내장되어있는 객체(embeddingModel)를 통해 임베딩을 해서 저장
    private void saveDocument(Long productId, List<String> keywords) {
        ProductDocument doc = new ProductDocument();
        doc.setId(productId);
        if (!keywords.isEmpty()) { // 키워드가 없으면 벡터도 없다 (검색 대상에서 빠짐)
            doc.setEmbedding(calculateAverage(embeddingModel.embed(keywords)));
        }
        productDocumentRepository.save(doc);
    }

    // 키워드마다 만든 벡터를 차원별로 평균 내어 상품 하나를 대표하는 벡터로 만든다
    private float[] calculateAverage(List<float[]> vectors) {
        int dimension = vectors.get(0).length;
        float[] sum = new float[dimension];

        for (float[] vector : vectors) {
            for (int i = 0; i < dimension; i++) {
                sum[i] += vector[i];
            }
        }

        for (int i = 0; i < dimension; i++) {
            sum[i] /= vectors.size();
        }

        return sum;
    }
}