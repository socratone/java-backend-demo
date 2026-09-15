package com.example.multidb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 자동 설정과 현재 패키지 아래의 컴포넌트 탐색을 활성화하는 애플리케이션 진입점입니다. */
@SpringBootApplication
public class MultiDbApplication {
    /** Spring 컨테이너와 내장 웹 서버를 시작합니다. */
    public static void main(String[] args) {
        SpringApplication.run(MultiDbApplication.class, args);
    }
}
