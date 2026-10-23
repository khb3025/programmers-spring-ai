package com.back;

import com.back.ai.service.ChatService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "120s")
class BackApplicationTests {
	@Autowired
	private FaqRepository faqRepository;
	@Autowired
	private FaqService faqService;
	@Autowired
	private WebTestClient webTestClient;
//	@Autowired
//	private ChatService chatService;

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("FAQ 게시글 초기화 테스트")
	void t1() {
		StepVerifier.create(faqRepository.count())
				.assertNext(count -> {
					System.out.println("FAQ 게시글 수: " + count);
					assertTrue(count >= 1);
				})
				.verifyComplete();
	}

	@Test
	@DisplayName("PGroonga FTS 검색 테스트")
	void t2() {
		StepVerifier.create(faqRepository.searchByKeyword("배송").collectList())
				.assertNext(results -> {
					assertFalse(results.isEmpty());
					assertTrue(results.stream()
							.anyMatch(faq -> faq.getQuestion().contains("배송") || faq.getAnswer().contains("배송")));
					System.out.println("FTS 검색 결과:");
					results.forEach(System.out::println);
				})
				.verifyComplete();
	}
//	@Test
//	@DisplayName("FAQ 전문 검색 테스트")
//	void t1(){
//		// given
//		String keyword = "배송";
//
//		// when
//		List<Faq> results = faqService.searchFaq(keyword);
//
//		// then
//		assertFalse(results.isEmpty());
//		assertTrue(results.stream().anyMatch(f ->
//				f.getQuestion().contains("배송") || f.getAnswer().contains("배송")));
//		System.out.println("FTS 검색 결과:");
//		results.forEach(System.out::println);
//	}

//	@Test
//	@DisplayName("FAQ 기반 AI 챗봇 테스트")
//	void t2(){
//		// given
//		String userMessage = "배송은 얼마나 걸려요?";
//
//		// when & then
//		String response = chatService.streamChatResponse(userMessage)
//				.collectList()
//				.map(list -> String.join("", list))
//				.block();
//
//		assertNotNull(response);
//		assertFalse(response.isEmpty());
//		System.out.println("AI 응답: " + response);
//	}

//	@Test
//	@DisplayName("FaqService searchFaq 테스트")
//	void t2_service() {
//		// FaqService는 내부에서 block()으로 동기 처리
//		List<Faq> results = faqService.searchFaq("배송");
//		assertFalse(results.isEmpty());
//	}

	@Test
	@DisplayName("FaqService searchFaq 테스트")
	void t2_service() {
		List<Faq> results = faqService.searchFaq("배송");
		assertFalse(results.isEmpty());
		assertTrue(results.stream()
				.anyMatch(faq -> faq.getQuestion().contains("배송") || faq.getAnswer().contains("배송")));
		System.out.println("FaqService 검색 결과:");
		results.forEach(System.out::println);
	}

	@Test
	@DisplayName("Ai Controller 테스트")
	void t3() {
		webTestClient.get()
				.uri(uri ->
						uri.path("/api/v1/ai/chat")
								.queryParam("message", "배송은 얼마나 걸리나요?")
								.build())
				.accept(MediaType.TEXT_EVENT_STREAM)
				.exchange()
				.expectStatus().isOk()
				.returnResult(String.class)
				.getResponseBody()
				.collectList()
				.map(list -> String.join("", list))
				.doOnNext(response -> {
					assertNotNull(response);
					assertFalse(response.isEmpty());
					System.out.println("AI 응답: " + response);
				})
				.block();
	}

	@Test
	@DisplayName("FAQ 저장 테스트")
	void t4() {
		Faq newFaq = Faq.builder()
				.question("테스트 질문입니다")
				.answer("테스트 답변입니다")
				.build();

		StepVerifier.create(faqRepository.save(newFaq))
				.assertNext(saved -> {
					assertNotNull(saved.getId());
					assertEquals("테스트 질문입니다", saved.getQuestion());
					assertEquals("테스트 답변입니다", saved.getAnswer());
					System.out.println("저장된 FAQ: " + saved);
				})
				.verifyComplete();
	}

	@Test
	@DisplayName("FAQ 전체 조회 테스트")
	void t5() {
		StepVerifier.create(faqRepository.findAll().collectList())
				.assertNext(faqs -> {
					assertFalse(faqs.isEmpty());
					System.out.println("전체 FAQ 수: " + faqs.size());
				})
				.verifyComplete();
	}

//	@Test
//	@DisplayName("SSE 스트리밍 API 테스트")
//	void t3(){
//		webTestClient.get()
//				.uri(uriBuilder -> uriBuilder
//						.path("/api/v1/ai/chat")
//						.queryParam("message", "환불 방법을 알려주세요")
//						.build())
//				.accept(MediaType.TEXT_EVENT_STREAM)
//				.exchange()
//				.expectStatus().isOk()
//				.returnResult(String.class)
//				.getResponseBody()
//				.collectList()
//				.map(list -> String.join("", list))
//				.doOnNext(response -> {
//					assertNotNull(response);
//					assertFalse(response.isEmpty());
//					System.out.println("SSE 응답: " + response);
//				})
//				.block();
//	}
}