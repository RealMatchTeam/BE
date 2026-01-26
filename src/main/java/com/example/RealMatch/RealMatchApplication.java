package com.example.RealMatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RealMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealMatchApplication.class, args);
    }

}
