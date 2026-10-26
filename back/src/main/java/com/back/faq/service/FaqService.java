package com.back.faq.service;

import com.back.faq.entity.Faq;
import com.back.faq.repository.FaqRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Service
public class FaqService {
    @Autowired
    private FaqRepository faqRepository;

    // 컨트롤러가 쓰는 검색 메서드. Flux를 그대로 돌려주므로 블로킹 없이 결과가 흘러간다.
    public Flux<Faq> search(String keyword) {
        return faqRepository.searchByKeyword(keyword);
    }

    /***
     * @Tool: Spring AI의 함수 호출(Function Calling) 어노테이션
     * @ToolParam: AI가 파라미터를 이해할 수 있도록 설명 제공
     * -> 리포지터리는 Flux를 돌려주지만, 도구는 실제 데이터(List)를 반환해야 한다. (10강에서 자세히 다룬다)
     *    도구가 Flux로 반환값을 설정하면, 데이터를 가지고 오지 않고 바로 반환될 수 있다.
     *
     *    - Flux를 그대로 반환하면 구독되지 않은 Flux 객체가 JSON으로 바뀌어 AI는 데이터를 받지 못한다.
     *    - 그래서 결과를 모아(List) 반환한다. block()은 블로킹이 허용된 스레드(boundedElastic 등)에서만 호출할 수 있다.
     */
    @Tool(description = "FAQ 데이터베이스에서 키워드로 질문과 답변을 검색합니다. 회원가입, 배송, 환불, 교환, 포인트, 쿠폰 등의 질문에 사용하세요.")
    public List<Faq> searchFaq(
            @ToolParam(description = "검색할 키워드 (예: 배송, 환불, 회원가입)") String keyword
    ) {

        return search(keyword)
                .collectList()
                .block(Duration.ofSeconds(10)); // DB가 응답하지 않을 때 무한정 기다리지 않게 한다
    };
}
