package com.emailagent.service;

import com.emailagent.model.DecisionResult;
import com.emailagent.model.EmailMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentService {

    @Value("${mail.imap.username}")
    private String emailUsername;

    private final EmailService emailService;
    private final RuleEngineService ruleEngineService;
    private final AIService aiService;
    private final ActionService actionService;
    private final LoggerService loggerService;

    // Hardcoded for now — becomes dynamic per user in SaaS
    private static final String USER_ID = "default_user";

    public AgentService(EmailService emailService,
                        RuleEngineService ruleEngineService,
                        AIService aiService,
                        ActionService actionService,
                        LoggerService loggerService) {
        this.emailService     = emailService;
        this.ruleEngineService = ruleEngineService;
        this.aiService        = aiService;
        this.actionService    = actionService;
        this.loggerService    = loggerService;
    }

    @Scheduled(fixedDelayString = "${agent.poll.interval.ms}")
    public void run() {
        System.out.println("\n🔄 Agent run started...");

        List<EmailMessage> emails = emailService.fetchUnreadEmails();
        System.out.println("📥 Fetched " + emails.size() + " emails");

        int newCount = 0;

        for (EmailMessage email : emails) {

            // Skip already processed emails
            if (loggerService.isAlreadyProcessed(USER_ID, email.getMessageId())) {
                System.out.println("⏩ Already processed: " + email.getSubject());
                continue;
            }
            // Skip emails sent by ourselves
            if (email.getFrom().contains(emailUsername)) {
                System.out.println("⏩ Skipping own email: " + email.getSubject());
                continue;
            }

            newCount++;
            System.out.println("\n📧 " + email.getSubject()
                    + " | from: " + email.getFrom());

            // Rule engine first
            DecisionResult decision = ruleEngineService.evaluate(email);

            // AI fallback
            if (decision == null) {
                System.out.println("→ No rule matched, asking AI...");
                decision = aiService.classify(email);
            } else {
                System.out.println("→ Rule matched: " + decision.getMatchedRuleName());
            }

            // Execute action
            boolean success = false;
            try {
                actionService.execute(email, decision);
                success = true;
            } catch (Exception e) {
                System.err.println("Action failed: " + e.getMessage());
            }

            // Log to database
            loggerService.log(USER_ID, email, decision, success);
        }

        System.out.println("\n✅ Run complete. New emails processed: " + newCount);
        loggerService.printStats(USER_ID);
    }
}