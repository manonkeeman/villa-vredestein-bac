package com.villavredestein;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VredesteinApplication {

    private static final Logger log = LoggerFactory.getLogger(VredesteinApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VredesteinApplication.class, args);
        log.info("🚀 Villa Vredestein backend is gestart met automatische jobs!");
    }
}