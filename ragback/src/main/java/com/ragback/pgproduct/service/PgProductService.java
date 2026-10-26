package com.ragback.pgproduct.service;

import com.ragback.pgproduct.entity.PgProduct;
import com.ragback.pgproduct.repository.PgProductRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Score;
import org.springframework.data.domain.ScoringFunction;
import org.springframework.data.domain.SearchResult;
import org.springframework.data.domain.SearchResults;
import org.springframework.data.domain.Vector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

// pgvector 학습용 서비스 : ProductService(Elasticsearch KNN)와 같은 기능을 PostgreSQL 하나로 처리한다
@Service
public class PgProductService {
    @Autowired
    private PgProductRepository pgProductRepository;
    @Autowired
    private EmbeddingModel embeddingModel;

    // 상품·키워드·벡터를 한 테이블(한 트랜잭션)에 저장한다.
    // Elasticsearch 방식과 달리 "DB에는 있지만 검색되지 않는 상품"이 생기지 않는다.
    public PgProduct create(String name, List<String> keywords) {
        PgProduct product = new PgProduct();
        product.setName(name);
        keywords.forEach(product::addKeyword);
        product.setEmbedding(embed(keywords));
        return pgProductRepository.save(product);
    }

    public Optional<PgProduct> findById(Long id) {
        return pgProductRepository.findById(id);
    }

    public List<PgProduct> findAll() {
        return StreamSupport.stream(pgProductRepository.findAll().spliterator(), false).toList();
    }

    @Transactional
    public PgProduct update(Long id, String name, List<String> keywords) {
        PgProduct product = pgProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PgProduct not found: " + id));

        product.setName(name);
        product.getKeywords().clear(); // orphanRemoval = true : 컬렉션에서 빠진 키워드는 DELETE 된다
        keywords.forEach(product::addKeyword);
        product.setEmbedding(embed(keywords));
        return product;
    }

    @Transactional
    public void delete(Long id) {
        pgProductRepository.deleteById(id); // 벡터도 같은 행에 있으므로 한 번에 지워진다
    }

    // 검색어를 임베딩해서, 코사인 거리가 가까운 상품 k개를 돌려준다
    public List<PgProduct> vectorSearch(String query, int k) {
        return findProductsByVector(embeddingModel.embed(query), k);
    }

    // 여러 키워드를 각각 임베딩한 뒤 평균 벡터로 검색한다 (상품 벡터를 만드는 방식과 같다)
    public List<PgProduct> vectorSearch(List<String> keywords, int k) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }
        return findProductsByVector(calculateAverage(embeddingModel.embed(keywords)), k);
    }

    // 기준 상품에 저장된 벡터로 바로 검색하고, 1위로 나오는 기준 상품 자신은 뺀다
    // (Elasticsearch 9.x와 달리 저장한 벡터를 그대로 읽을 수 있으므로 다시 임베딩하지 않는다)
    @Transactional(readOnly = true)
    public List<PgProduct> findSimilarProducts(Long productId, int k) {
        PgProduct product = pgProductRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("PgProduct not found: " + productId));

        if (product.getEmbedding() == null) {
            return List.of();
        }

        return findProductsByVector(product.getEmbedding(), k + 1).stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(k)
                .toList();
    }

    private List<PgProduct> findProductsByVector(float[] queryVector, int k) {
        SearchResults<PgProduct> results = pgProductRepository.searchByEmbeddingNear(
                Vector.of(queryVector),
                // cosine()의 Score는 "코사인 거리(embedding <=> 쿼리)의 상한"이다. 거리는 0(같음) ~ 2(반대).
                // 2.0이면 거르지 않고 가까운 순서로 k개를 받는다. 0.5처럼 주면 그보다 먼 상품은 빠진다.
                Score.of(2.0, ScoringFunction.cosine()),
                Limit.of(k));
        results.forEach(r -> System.out.println("  pgvector hit " + r.getContent().getName() + " distance=" + r.getScore().getValue()));
        // Elasticsearch 방식과 달리 결과가 이미 엔티티이므로 findAllById + 재정렬이 필요 없다
        return results.stream().map(SearchResult::getContent).toList();
    }

    // 키워드가 없으면 벡터도 없다 (검색 대상에서 빠짐)
    private float[] embed(List<String> keywords) {
        return keywords.isEmpty() ? null : calculateAverage(embeddingModel.embed(keywords));
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
