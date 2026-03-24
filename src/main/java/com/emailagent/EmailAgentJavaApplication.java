package com.emailagent;

import com.emailagent.model.EmailMessage;
import com.emailagent.service.EmailService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class EmailAgentJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailAgentJavaApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(EmailService emailService) {
        return args -> {
            System.out.println("=== AI Email Agent — Phase 1: Fetching Emails ===");
            List<EmailMessage> emails = emailService.fetchUnreadEmails();
            System.out.println("Fetched " + emails.size() + " emails:\n");
            emails.forEach(System.out::println);
        };
    }
}