package com.back.global.initData;


import com.back.faq.entity.Faq;
import com.back.faq.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class BaseInitData implements ApplicationRunner {
    @Autowired
    private FaqRepository faqRepository;

    // 확장·테이블·인덱스는 schema/faq-schema.sql(spring.sql.init)이 만든다. 여기서는 데이터만 넣는다.
    @Override
    public void run(ApplicationArguments args) {
        initData()
                .block(); // 애플리케이션 시작(main 스레드) 단계라 기다려도 된다. 데이터가 준비된 뒤 서버가 요청을 받는다.
    }

    private Mono<Void> initData() {
        List<Faq> faqs = List.of(
                Faq.builder().question("회원가입은 어떻게 하나요?").answer("홈페이지 상단의 회원가입 버튼을 클릭하여 이메일, 비밀번호, 이름을 입력하시면 됩니다.").build(),
                Faq.builder().question("비밀번호를 잊어버렸어요").answer("로그인 페이지에서 '비밀번호 찾기'를 클릭하시고 가입 시 사용한 이메일을 입력하시면 비밀번호 재설정 링크가 발송됩니다.").build(),
                Faq.builder().question("회원 탈퇴는 어떻게 하나요?").answer("마이페이지 > 설정 > 회원탈퇴 메뉴에서 탈퇴를 진행하실 수 있습니다. 탈퇴 시 모든 데이터가 삭제됩니다.").build(),
                Faq.builder().question("배송은 얼마나 걸리나요?").answer("일반 배송은 2-3일, 빠른 배송은 익일 도착입니다. 도서산간 지역은 1-2일 추가될 수 있습니다.").build(),
                Faq.builder().question("배송비는 얼마인가요?").answer("3만원 이상 구매 시 무료배송이며, 3만원 미만은 2,500원의 배송비가 부과됩니다.").build(),
                Faq.builder().question("배송 조회는 어디서 하나요?").answer("마이페이지 > 주문내역에서 배송조회 버튼을 클릭하시면 실시간 배송 위치를 확인하실 수 있습니다.").build(),
                Faq.builder().question("환불은 어떻게 하나요?").answer("마이페이지 > 주문내역에서 환불신청 버튼을 클릭하시면 됩니다. 수령 후 7일 이내 신청 가능합니다.").build(),
                Faq.builder().question("환불 처리 기간은 얼마나 걸리나요?").answer("환불 신청 후 상품 회수까지 2-3일, 회수 확인 후 환불까지 3-5 영업일이 소요됩니다.").build(),
                Faq.builder().question("교환은 어떻게 하나요?").answer("마이페이지 > 주문내역에서 교환신청 버튼을 클릭하시면 됩니다. 동일 상품 사이즈/색상 교환만 가능합니다.").build(),
                Faq.builder().question("포인트는 어떻게 사용하나요?").answer("결제 시 포인트 사용 체크박스를 선택하시면 보유 포인트가 자동 적용됩니다. 1포인트 = 1원입니다.").build(),
                Faq.builder().question("포인트 적립률은 얼마인가요?").answer("구매 금액의 1%가 기본 적립되며, 등급별로 최대 5%까지 추가 적립됩니다.").build(),
                Faq.builder().question("쿠폰은 어디서 받나요?").answer("이벤트 페이지 또는 마이페이지 > 쿠폰함에서 발급받으실 수 있습니다.").build()
        );

        return faqRepository.count()
                .flatMap(count -> {
                    if (count > 0) {
                        System.out.println("FAQ 데이터가 이미 존재합니다: " + count + "건");
                        return Mono.empty();
                    }
                    return faqRepository.saveAll(faqs) // Flux<Faq> : 저장된 FAQ가 순서대로 흘러나온다
                            .then()                    // 결과는 버리고 "끝났다"는 신호만 남긴다
                            .doOnSuccess(v -> System.out.println("FAQ 초기 데이터 " + faqs.size() + "건 생성"));
                });
    }
}