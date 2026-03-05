package com.onnyth.onnythserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnnythServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnnythServerApplication.class, args);
    }

}
