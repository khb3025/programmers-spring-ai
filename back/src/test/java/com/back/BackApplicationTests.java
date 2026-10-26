package com.back;

import com.back.faq.entity.Faq;
import com.back.faq.repository.FaqRepository;
import com.back.faq.service.FaqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test") // 테스트는 Ollama 사용, OpenAI 자동 설정 제외
class BackApplicationTests {
	@Autowired
	private DatabaseClient databaseClient; // R2DBC용 SQL 실행 도구 (JdbcTemplate 역할)
	@Autowired
	private FaqRepository faqRepository;

	@Test
	@DisplayName("테스트 DB(PGroonga 이미지)에서 pgroonga 확장을 설치할 수 있다")
	void t0() {
		StepVerifier.create(databaseClient
						.sql("SELECT COUNT(*) FROM pg_available_extensions WHERE name = 'pgroonga'")
						.map(row -> row.get(0, Long.class))
						.one())
				.expectNext(1L)
				.verifyComplete();
	}

	@Test
	@DisplayName("애플리케이션 시작 시 pgroonga 확장이 설치되고 FAQ 12건이 생성된다")
	void t1() {
		StepVerifier.create(databaseClient
						.sql("SELECT COUNT(*) FROM pg_extension WHERE extname = 'pgroonga'")
						.map(row -> row.get(0, Long.class))
						.one())
				.expectNext(1L)
				.verifyComplete();

		StepVerifier.create(faqRepository.count())
				.expectNext(12L)
				.verifyComplete();
	}

	@Test
	@DisplayName("PGroonga 전문 검색 - '배송'이 들어간 FAQ만 찾는다")
	void t2() {
		StepVerifier.create(faqRepository.searchByKeyword("배송").collectList())
				.assertNext(results -> {
					assertEquals(3, results.size());
					assertTrue(results.stream().allMatch(f ->
							f.getQuestion().contains("배송") || f.getAnswer().contains("배송")));
					System.out.println("FTS 검색 결과:");
					results.forEach(System.out::println);
				})
				.verifyComplete();
	}

	@Test
	@DisplayName("PGroonga 쿼리 문법 - 'OR'로 여러 키워드를 한 번에 검색한다")
	void t3() {
		StepVerifier.create(faqRepository.searchByKeyword("배송 OR 환불"))
				.expectNextCount(5) // 요소 5개가 오고
				.verifyComplete();  // 정상 종료
	}

	@Test
	@DisplayName("PGroonga 인덱스가 생성되어 있다")
	void t4() {
		StepVerifier.create(databaseClient
						.sql("SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'pgroonga_faq_index'")
						.map(row -> row.get(0, Long.class))
						.one())
				.expectNext(1L)
				.verifyComplete();
	}

	@Test
	@DisplayName("R2DBC 저장 - id가 null인 엔티티를 저장하면 DB가 id를 만든다")
	void t5() {
		Faq newFaq = Faq.builder().question("테스트 질문입니다").answer("테스트 답변입니다").build();

		StepVerifier.create(faqRepository.save(newFaq)
						.flatMap(saved -> faqRepository.deleteById(saved.getId()).thenReturn(saved))) // 다른 테스트에 영향이 없게 지운다
				.assertNext(saved -> {
					assertNotNull(saved.getId());
					assertEquals("테스트 질문입니다", saved.getQuestion());
				})
				.verifyComplete();
	}
}