package com.back.global.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AiConfig {

    @Bean
    @Profile("default")
    public ChatClient openaiChatClient(ChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultSystem("""
                    당신은 FAQ 고객 지원 챗봇입니다.
                    사용자의 질문에 친절하게 답변하세요.
                    항상 한국어로 답변하세요.
                    """)
                .build();
    }

    @Bean
    @Profile("test")
    public ChatClient ollamaChatClient(ChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .defaultSystem("""
                    당신은 FAQ 고객 지원 챗봇입니다.
                    사용자의 질문에 친절하게 답변하세요.
                    항상 한국어로 답변하세요.
                    """)
                .build();
    }
}