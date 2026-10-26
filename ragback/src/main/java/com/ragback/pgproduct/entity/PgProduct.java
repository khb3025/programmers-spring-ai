package com.ragback.pgproduct.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

// pgvector 학습용 상품 엔티티 (테이블 : pg_product)
// Elasticsearch용 Product와 달리, 벡터를 별도 저장소가 아닌 같은 테이블의 embedding 컬럼에 저장한다
@Entity
public class PgProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column
    @Getter
    @Setter
    private String name;

    // mappedBy : 외래키(pg_product_id)는 PgProductKeyword.product 쪽이 관리한다(연관관계의 주인)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private List<PgProductKeyword> keywords = new ArrayList<>();

    // pgvector의 vector(384) 컬럼. 384는 임베딩 차원(AiConfig.EMBEDDING_DIMENSIONS)과 같아야 한다
    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 384)
    @Getter
    @Setter
    @JsonIgnore // 숫자 384개를 API 응답에 싣지 않는다
    private float[] embedding;

    // 연관관계 편의 메서드 : 양쪽을 한 번에 연결한다
    public void addKeyword(PgProductKeyword keyword) {
        keywords.add(keyword);
        keyword.setProduct(this);
    }

    public void removeKeyword(PgProductKeyword keyword) {
        keywords.remove(keyword);
        keyword.setProduct(null);
    }

    public void addKeyword(String keywordText) {
        addKeyword(new PgProductKeyword(keywordText));
    }
}
