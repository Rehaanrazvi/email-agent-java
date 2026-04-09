package com.emailagent.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_emails")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SaaS-ready — userId for multi-user later
    private String userId;

    private String emailId;
    private String subject;
    private String fromAddress;
    private String action;
    private String decisionSource; // RULE or AI
    private Double confidence;
    private String matchedRule;
    private LocalDateTime processedAt;
    private boolean success;

    public ProcessedEmail(String userId, String emailId, String subject,
                          String fromAddress, String action, String decisionSource,
                          Double confidence, String matchedRule, boolean success) {
        this.userId        = userId;
        this.emailId       = emailId;
        this.subject       = subject;
        this.fromAddress   = fromAddress;
        this.action        = action;
        this.decisionSource = decisionSource;
        this.confidence    = confidence;
        this.matchedRule   = matchedRule;
        this.processedAt   = LocalDateTime.now();
        this.success       = success;
    }
}