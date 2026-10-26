package com.back.ai;

import com.back.ai.service.ChatService;
import com.back.faq.service.FaqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class ToolBlockingTests {
    @Autowired
    private FaqService faqService;
    @Autowired
    private ChatService chatService;

    // 비교용 : 리포지터리의 Flux를 그대로 반환하는 잘못된 도구
    static class FluxReturningTool {
        @Tool(description = "Flux를 그대로 반환하는 도구")
        public Flux<String> search(String keyword) {
            return Flux.just("배송은 얼마나 걸리나요?", "배송비는 얼마인가요?");
        }
    }

    @Test
    @DisplayName("도구가 Flux를 반환하면 AI는 데이터가 아니라 Flux 객체의 JSON을 받는다")
    void t1() {
        String result = ToolCallbacks.from(new FluxReturningTool())[0].call("{\"keyword\":\"배송\"}");
        System.out.println("Flux 반환 도구의 결과: " + result);

        assertFalse(result.contains("배송은 얼마나 걸리나요?"));
        assertTrue(result.contains("prefetch"));
    }

    @Test
    @DisplayName("논블로킹 스레드(parallel)에서 도구를 직접 실행하면 block()이 거부된다")
    void t2() {
        Mono<Integer> onParallel = Mono.fromCallable(() -> faqService.searchFaq("배송").size())
                .subscribeOn(Schedulers.parallel()); // WebFlux 요청 처리 스레드와 같은 논블로킹 스레드

        StepVerifier.create(onParallel)
                .expectErrorSatisfies(e -> {
                    System.out.println("parallel 스레드에서의 에러: " + e.getMessage());
                    assertInstanceOf(IllegalStateException.class, e);
                    assertTrue(e.getMessage().contains("block()/blockFirst()/blockLast() are blocking"));
                })
                .verify();
    }

    @Test
    @DisplayName("블로킹이 허용된 스레드(boundedElastic)에서는 도구가 정상 동작한다")
    void t3() {
        Mono<Integer> onBoundedElastic = Mono.fromCallable(() -> faqService.searchFaq("배송").size())
                .subscribeOn(Schedulers.boundedElastic());

        StepVerifier.create(onBoundedElastic)
                .expectNext(3)
                .verifyComplete();
    }

    @Test
    @DisplayName("chat()은 논블로킹 스레드에서 구독해도 boundedElastic으로 옮겨 실행되므로 동작한다")
    void t4() {
        Mono<String> fromParallel = Mono.defer(() -> chatService.chat("배송비는 얼마인가요?"))
                .subscribeOn(Schedulers.parallel());

        StepVerifier.create(fromParallel)
                .assertNext(answer -> {
                    assertFalse(answer.isEmpty());
                    System.out.println("AI 응답: " + answer);
                })
                .verifyComplete();
    }
}