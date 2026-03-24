package com.emailagent;

import com.emailagent.model.EmailMessage;
import com.emailagent.service.EmailService;
import com.emailagent.service.SmtpService;
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
    public CommandLineRunner run(EmailService emailService, SmtpService smtpService) {
        return args -> {

            // Phase 1 — Read emails
            System.out.println("=== Phase 1: Fetching Emails ===");
            List<EmailMessage> emails = emailService.fetchUnreadEmails();
            System.out.println("Fetched " + emails.size() + " emails\n");
            emails.forEach(System.out::println);

            // Phase 2 — Send a test email
            System.out.println("\n=== Phase 2: Sending Test Email ===");
            smtpService.sendEmail(
                    "rehanrazvi222@gmail.com",   // send to yourself for testing
                    "Test from AI Email Agent",
                    "Hello! This email was sent automatically by your AI Email Agent. Phase 2 is working!"
            );
        };
    }
}