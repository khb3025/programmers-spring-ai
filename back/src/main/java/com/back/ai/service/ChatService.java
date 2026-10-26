package com.back.ai.service;

import com.back.faq.service.FaqService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ChatService {
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private FaqService faqService;

    private static final String SYSTEM_PROMPT = """
            당신은 친절한 고객 서비스 챗봇입니다.
            사용자의 질문에 대해 FAQ 데이터베이스를 검색하여 정확한 답변을 제공하세요.
            검색 결과가 없으면 "해당 내용에 대한 FAQ가 없습니다. 고객센터(1234-5678)로 문의해주세요."라고 안내하세요.
            """;

//    public String chat(String userMessage) {
//        return chatClient.prompt()
//                .system(SYSTEM_PROMPT)
//                .messages(UserMessage.builder().text(userMessage).build())
//                .tools(faqService) // FaqService의 @Tool 메서드를 AI가 호출할 수 있게 등록
//                .call()
//                .content();

    // call()은 답변이 끝날 때까지 기다리는 블로킹 호출이고, 도구도 호출한 스레드에서 실행된다.
    // 그래서 블로킹이 허용된 boundedElastic 스레드로 옮겨서 실행하고 결과를 Mono로 돌려준다.
    public Mono<String> chat(String userMessage) {
        return Mono.fromCallable(() -> chatClient.prompt()
                        .system(SYSTEM_PROMPT)
                        .messages(UserMessage.builder().text(userMessage).build())
                        .tools(faqService)
                        .call()
                        .content())
                .subscribeOn(Schedulers.boundedElastic());
    }

    // stream()에서는 Spring AI가 도구를 boundedElastic 스레드에서 실행한다.
    public Flux<String> streamChatResponse(String userMessage){
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .messages(UserMessage.builder().text(userMessage).build())
                .tools(faqService)
                .stream()
                .content();
    }
}