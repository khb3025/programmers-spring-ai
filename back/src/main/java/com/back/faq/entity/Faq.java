package com.back.faq.entity;

/*import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;*/
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

// @Entity
@Table("faq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Faq {

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(nullable = false, length = 500) -> R2DBC 는 DDL 기능이 없음으로 자료형 정의가 의미 없음
    // @Column("question") -> 어차피 쿼리를 작성하여 테이블을 생성함으로 크게 의미 없음.
    private String question;

    // @Column(nullable = false, length = 2000)
    // @Column("answer")
    private String answer;
}