package com.Back;

import com.Back.ai.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // application-test.yml 설정 파일 사용
@AutoConfigureWebTestClient // WebTestClien 자동 구성
class BackApplicationTests {
	@Autowired
	private ChatService chatService;
	@Autowired
	private WebTestClient webTestClient;

	@Test
	@DisplayName("Ollama Chat 클라이언트 테스트")
	void t1(){
		assertDoesNotThrow(()->{
			String response = chatService.streamChatResponse("Hello, Ollama!")
					.collectList()
					.map(list -> String.join("", list))
					.block();
			assertNotNull(response);
			assertFalse(response.isEmpty());
			System.out.println("Ollama Response: " + response);
		});
	}

	@Test
	@DisplayName("SSE 스트리밍 테스트")
	void t2(){
		assertDoesNotThrow(()->{
			webTestClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/api/v1/ai/chat")
							.queryParam("message", "Hello, SSE!")
							.build())
					// 한 번에 스트림을 다 주지 말고 조금씩 보내라
					.accept(MediaType.TEXT_EVENT_STREAM)
					// 요청을 실제로 보냅니다.
					// 응답 상태가 200 OK인지 검증하고, 아니면 바로 실패합니다.
					.exchange()
					.expectStatus().isOk()
					.returnResult(String.class)
					.getResponseBody()
					// 응답 본문을 Flux<String>으로 받습니다. AI가 토큰(글자 조각)을 하나씩 보낼 때마다 "Hel", "lo", "!"처럼 원소가 하나씩 흘러옵니다.
					.collectList()
					.map(list -> String.join("", list))
					// 흘러온 조각을 스트림이 끝날 때까지 모아 List<String> 으로 만듭니다.
					// 그 리스트를 이어 붙여 문장 하나(String)로 만듭니다.
					.doOnNext(response -> {
						assertNotNull(response);
						assertFalse(response.isEmpty());
						System.out.println("SSE Response: " + response);
					})
					//완성된 문장이 null이 아니고 비어 있지 않은지 검증합니다.
					//콘솔에 응답을 출력합니다.
					.block();
		});
	}


}
