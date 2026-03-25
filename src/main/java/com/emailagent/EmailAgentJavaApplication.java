package com.emailagent;

import com.emailagent.model.DecisionResult;
import com.emailagent.model.EmailMessage;
import com.emailagent.service.EmailService;
import com.emailagent.service.RuleEngineService;
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
    public CommandLineRunner run(EmailService emailService,
                                 SmtpService smtpService,
                                 RuleEngineService ruleEngineService) {
        return args -> {

            // Phase 1 — Read emails
            System.out.println("=== Phase 1: Fetching Emails ===");
            List<EmailMessage> emails = emailService.fetchUnreadEmails();
            System.out.println("Fetched " + emails.size() + " emails\n");

            // Phase 3 — Apply rules
            System.out.println("=== Phase 3: Applying Rules ===");
            for (EmailMessage email : emails) {
                System.out.println("Processing: " + email.getSubject());
                DecisionResult decision = ruleEngineService.evaluate(email);
                if (decision != null) {
                    System.out.println(decision);
                } else {
                    System.out.println("→ No rule matched for: " + email.getSubject());
                }
            }
        };
    }
}