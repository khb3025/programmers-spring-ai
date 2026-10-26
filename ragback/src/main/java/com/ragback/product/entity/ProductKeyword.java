package com.ragback.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
public class ProductKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Setter
    @Getter
    private Product product;

    @Column
    @Getter
    @Setter
    private String keyword;

    // JPA는 엔티티 생성 시 기본 생성자가 필요하다
    protected ProductKeyword() {
    }

    public ProductKeyword(String keyword) {
        this.keyword = keyword;
    }
}