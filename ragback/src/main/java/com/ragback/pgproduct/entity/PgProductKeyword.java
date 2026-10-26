package com.ragback.pgproduct.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// pgvector 학습용 상품 키워드 엔티티 (테이블 : pg_product_keyword)
@Entity
public class PgProductKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Setter
    @Getter
    @JsonIgnore // PgProduct -> keywords -> product -> keywords ... 무한 반복(순환 참조)을 끊는다
    private PgProduct product;

    @Column
    @Getter
    @Setter
    private String keyword;

    // JPA는 엔티티 생성 시 기본 생성자가 필요하다
    protected PgProductKeyword() {
    }

    public PgProductKeyword(String keyword) {
        this.keyword = keyword;
    }
}
