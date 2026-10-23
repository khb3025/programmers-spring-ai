package com.Back.ai.controller;

import com.Back.ai.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai/chat")
public class AiController {
    @Autowired
    private ChatService chatService;

    /***
     * GET /api/v1/ai/chat?message={message}: AI 채팅 스트리밍 응답
     *
     * produces = MediaType.TEXT_EVENT_STREAM_VALUE 설정 시
     * Spring WebFlux가 Content-Type: text/event-stream 헤더를 자동 설정하고 SSE 형식으로 응답 전송
     *
     * Flux<String> 반환 타입으로 자동 SSE 스트리밍
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String message){
        return chatService.streamChatResponse(message);
    }
}
