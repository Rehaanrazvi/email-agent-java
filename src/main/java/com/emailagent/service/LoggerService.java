package com.emailagent.service;

import com.emailagent.model.DecisionResult;
import com.emailagent.model.EmailMessage;
import com.emailagent.model.ProcessedEmail;
import com.emailagent.repository.ProcessedEmailRepository;
import org.springframework.stereotype.Service;

@Service
public class LoggerService {

    private final ProcessedEmailRepository repository;

    public LoggerService(ProcessedEmailRepository repository) {
        this.repository = repository;
    }

    public boolean isAlreadyProcessed(String userId, String emailId) {
        return repository.existsByUserIdAndEmailId(userId, emailId);
    }

    public void log(String userId, EmailMessage email,
                    DecisionResult decision, boolean success) {
        ProcessedEmail log = new ProcessedEmail(
                userId,
                email.getMessageId(),
                email.getSubject(),
                email.getFrom(),
                decision.getAction(),
                decision.getDecisionSource(),
                null,
                decision.getMatchedRuleName(),
                success
        );
        repository.save(log);
        System.out.println("📝 Logged: " + email.getSubject()
                + " → " + decision.getAction());
    }

    public void printStats(String userId) {
        long total   = repository.countByUserId(userId);
        long byRule  = repository.countByUserIdAndDecisionSource(userId, "RULE");
        long byAI    = repository.countByUserIdAndDecisionSource(userId, "AI");
        long replied = repository.countByUserIdAndAction(userId, "reply");
        long ignored = repository.countByUserIdAndAction(userId, "ignore");

        System.out.println("\n📊 Stats for user: " + userId);
        System.out.println("Total processed : " + total);
        System.out.println("By Rule         : " + byRule);
        System.out.println("By AI           : " + byAI);
        System.out.println("Replied         : " + replied);
        System.out.println("Ignored         : " + ignored);
    }
}