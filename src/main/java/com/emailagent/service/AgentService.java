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

        List<EmailMessage> allEmails = emailService.fetchUnreadEmails();
        System.out.println("📥 Fetched " + allEmails.size() + " emails from Gmail");

        // Filter processed + own emails → take 10 genuinely new ones
        List<EmailMessage> newEmails = allEmails.stream()
                .filter(email -> !loggerService.isAlreadyProcessed(
                        USER_ID, email.getMessageId()))
                .filter(email -> !email.getFrom().contains(emailUsername))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        System.out.println("🆕 New emails to process: " + newEmails.size());

        if (newEmails.isEmpty()) {
            System.out.println("✅ Nothing new to process.");
            loggerService.printStats(USER_ID);
            return;
        }

        for (EmailMessage email : newEmails) {
            System.out.println("\n📧 " + email.getSubject()
                    + " | from: " + email.getFrom());

            DecisionResult decision = ruleEngineService.evaluate(email);

            if (decision == null) {
                System.out.println("→ No rule matched, asking AI...");
                decision = aiService.classify(email);
            } else {
                System.out.println("→ Rule matched: "
                        + decision.getMatchedRuleName());
            }

            boolean success = false;
            try {
                actionService.execute(email, decision);
                success = true;
            } catch (Exception e) {
                System.err.println("Action failed: " + e.getMessage());
            }

            loggerService.log(USER_ID, email, decision, success);
        }

        System.out.println("\n✅ Run complete. Processed: " + newEmails.size());
        loggerService.printStats(USER_ID);
    }
}