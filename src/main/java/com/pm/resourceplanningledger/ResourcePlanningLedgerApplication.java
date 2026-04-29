package com.pm.resourceplanningledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.Clock;

@SpringBootApplication
public class ResourcePlanningLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourcePlanningLedgerApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}