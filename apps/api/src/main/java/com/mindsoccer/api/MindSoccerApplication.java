package com.mindsoccer.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
    "com.mindsoccer.api",
    "com.mindsoccer.match",
    "com.mindsoccer.content",
    "com.mindsoccer.scoring",
    "com.mindsoccer.anticheat",
    "com.mindsoccer.shared",
    "com.mindsoccer.realtime"
})
@EnableAsync
@EntityScan(basePackages = "com.mindsoccer")
@EnableJpaRepositories(basePackages = "com.mindsoccer")
public class MindSoccerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindSoccerApplication.class, args);
    }
}
