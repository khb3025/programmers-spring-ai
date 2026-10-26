package com.ragback;

import org.springframework.boot.SpringApplication;

public class TestBackApplication {

    public static void main(String[] args) {
        SpringApplication.from(RagbackApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}