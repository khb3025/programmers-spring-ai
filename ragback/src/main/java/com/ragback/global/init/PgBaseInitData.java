package com.ragback.global.init;

import com.ragback.pgproduct.service.PgProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

// pgvector 학습용 초기 데이터 (BaseInitData와 같은 상품을 pg_product 테이블에 넣는다)
@Configuration
@RequiredArgsConstructor
public class PgBaseInitData {

    private final PgProductService pgProductService;

    @Bean
    @Profile("!test") // 테스트에서는 각 테스트가 필요한 데이터를 직접 만든다
    public ApplicationRunner pgInitDataRunner() {
        return args -> {
            if (!pgProductService.findAll().isEmpty()) {
                return;
            }

            // Electronics
            pgProductService.create("MacBook Pro 16", List.of("laptop", "apple", "development", "programming"));
            pgProductService.create("MacBook Air M3", List.of("laptop", "apple", "lightweight", "portable"));
            pgProductService.create("Dell XPS 15", List.of("laptop", "windows", "development", "high-performance"));
            pgProductService.create("iPhone 15 Pro", List.of("smartphone", "apple", "camera", "5G"));
            pgProductService.create("Samsung Galaxy S24", List.of("smartphone", "android", "camera", "AI"));
            pgProductService.create("iPad Pro 12.9", List.of("tablet", "apple", "drawing", "creative"));
            pgProductService.create("Sony WH-1000XM5", List.of("headphone", "wireless", "noise-cancelling", "music"));
            pgProductService.create("AirPods Pro 2", List.of("earbuds", "apple", "wireless", "noise-cancelling"));

            // Clothing
            pgProductService.create("North Face Puffer Jacket", List.of("outerwear", "winter", "warm", "outdoor"));
            pgProductService.create("Patagonia Fleece", List.of("outerwear", "fleece", "outdoor", "hiking"));
            pgProductService.create("Levi's 501 Jeans", List.of("pants", "denim", "casual", "classic"));
            pgProductService.create("Nike Air Max 90", List.of("shoes", "sneakers", "running", "sports"));
            pgProductService.create("Adidas Ultraboost", List.of("shoes", "running", "sports", "comfortable"));

            // Food & Beverage
            pgProductService.create("Starbucks Pike Place Roast", List.of("coffee", "roasted", "beverage", "caffeine"));
            pgProductService.create("Twinings Earl Grey Tea", List.of("tea", "earl-grey", "beverage", "british"));
            pgProductService.create("Coca-Cola Zero", List.of("soda", "beverage", "sugar-free", "refreshing"));

            // Home & Kitchen
            pgProductService.create("Dyson V15 Vacuum", List.of("vacuum", "cleaning", "home", "cordless"));
            pgProductService.create("Instant Pot Duo", List.of("cooker", "kitchen", "pressure", "multi-function"));
            pgProductService.create("Nespresso Vertuo", List.of("coffee-machine", "kitchen", "espresso", "capsule"));

            // Books & Media
            pgProductService.create("Clean Code Book", List.of("book", "programming", "software", "education"));

            System.out.println("=== Initial pgvector data created: 20 products ===");
        };
    }
}
