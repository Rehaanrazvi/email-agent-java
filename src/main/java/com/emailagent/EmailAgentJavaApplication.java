package com.emailagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmailAgentJavaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmailAgentJavaApplication.class, args);
    }
}