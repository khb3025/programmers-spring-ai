package com.back.faq.controller;

import com.back.faq.entity.Faq;
import com.back.faq.repository.FaqRepository;
import com.back.faq.service.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/faqs")
public class FaqController {
    @Autowired
    private FaqService faqService;

    // Flux를 반환하면 쿼리 결과가 도착할 때 응답을 쓴다. 기다리는 동안 요청 처리 스레드는 다른 요청을 처리한다.
    public Flux<Faq> search(@RequestParam String keyword) {
        System.out.println("[" + Thread.currentThread().getName() + "] FAQ 검색 (R2DBC) : " + keyword);
        return faqService.search(keyword);
    }
}