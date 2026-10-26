package com.ragback.pgproduct;

import com.ragback.TestcontainersConfiguration;
import com.ragback.pgproduct.entity.PgProduct;
import com.ragback.pgproduct.repository.PgProductRepository;
import com.ragback.pgproduct.service.PgProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class PgProductSearchTests {
    @Autowired
    private PgProductService pgProductService;
    @Autowired
    private PgProductRepository pgProductRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PgProduct gamingLaptop;

    // 테스트 클래스들이 같은 컨테이너를 공유하므로, 검색 결과가 섞이지 않게 비우고 시작한다
    @BeforeEach
    void setUp() {
        pgProductRepository.deleteAll();

        gamingLaptop = pgProductService.create("Gaming Laptop", List.of("gaming", "laptop", "graphics-card"));
        pgProductService.create("Business Laptop", List.of("office", "document", "lightweight"));
        pgProductService.create("Gaming Mouse", List.of("gaming", "mouse", "RGB"));
        pgProductService.create("Espresso Coffee", List.of("coffee", "espresso", "beverage"));
        // Elasticsearch와 달리 refresh가 필요 없다 : 커밋되면 바로 검색된다
    }

    @Test
    @DisplayName("pgvector - pg_product.embedding 컬럼이 vector(384) 타입이다")
    void t0() {
        String type = jdbcTemplate.queryForObject("""
                SELECT format_type(atttypid, atttypmod) FROM pg_attribute
                WHERE attrelid = 'pg_product'::regclass AND attname = 'embedding'
                """, String.class);
        assertEquals("vector(384)", type);
    }

    @Test
    @DisplayName("pgvector 검색 - 'gaming' 과 가장 가까운 상품은 게이밍 상품이다")
    void t1() {
        List<PgProduct> results = pgProductService.vectorSearch("gaming", 2);

        System.out.println("gaming 검색 결과 : " + results.stream().map(PgProduct::getName).toList());
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.getName().startsWith("Gaming")));
    }

    @Test
    @DisplayName("pgvector 검색 - 키워드에 없는 단어(커피 음료)도 의미로 찾는다")
    void t2() {
        List<PgProduct> results = pgProductService.vectorSearch("hot drink in the morning", 1);

        System.out.println("hot drink 검색 결과 : " + results.stream().map(PgProduct::getName).toList());
        assertEquals("Espresso Coffee", results.get(0).getName());
    }

    @Test
    @DisplayName("pgvector 검색 - k개만 돌려준다")
    void t3() {
        assertEquals(3, pgProductService.vectorSearch("computer", 3).size());
    }

    @Test
    @DisplayName("키워드 목록 검색 - 여러 키워드의 평균 벡터로 찾는다")
    void t4() {
        List<PgProduct> results = pgProductService.vectorSearch(List.of("coffee", "espresso"), 1);

        assertEquals("Espresso Coffee", results.get(0).getName());
    }

    @Test
    @DisplayName("유사 상품 - 저장된 벡터로 찾고, 기준 상품 자신은 빠지고, 가장 비슷한 상품이 1위다")
    void t5() {
        List<PgProduct> similar = pgProductService.findSimilarProducts(gamingLaptop.getId(), 2);

        System.out.println("Gaming Laptop 유사 상품 : " + similar.stream().map(PgProduct::getName).toList());
        assertEquals(2, similar.size());
        assertTrue(similar.stream().noneMatch(p -> p.getId().equals(gamingLaptop.getId())));
        assertEquals("Gaming Mouse", similar.get(0).getName());
    }
}
