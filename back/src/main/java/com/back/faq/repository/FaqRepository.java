package com.back.faq.repository;


import com.back.faq.entity.Faq;
// import org.springframework.data.jpa.repository.Query;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

import java.util.List;

// public interface FaqRepository extends CrudRepository<Faq, Long> {
public interface FaqRepository extends R2dbcRepository<Faq, Long> {
    // &@~: PGroonga의 전문 검색 연산자 (한국어 형태소 분석 지원)
//    @Query(value = """
//        SELECT * FROM faq
//        WHERE question &@~ :keyword
//           OR answer &@~ :keyword
//        """, nativeQuery = true)
//    List<Faq> searchByKeyword(@Param("keyword") String keyword);

    @Query("""
        SELECT * FROM faq
        WHERE pgroonga_match_text(question, :keyword)
           OR pgroonga_match_text(answer, :keyword)
        """)
    Flux<Faq> searchByKeyword(@Param("keyword") String keyword);

}