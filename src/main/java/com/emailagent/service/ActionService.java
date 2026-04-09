package com.emailagent.service;

import com.emailagent.model.DecisionResult;
import com.emailagent.model.EmailMessage;
import org.springframework.stereotype.Service;

@Service
public class ActionService {

    private final SmtpService smtpService;

    public ActionService(SmtpService smtpService) {
        this.smtpService = smtpService;
    }

    public void execute(EmailMessage email, DecisionResult decision) {
        System.out.println("\n⚡ Executing action: " + decision.getAction()
                + " for: " + email.getSubject());

        switch (decision.getAction().toLowerCase()) {
            case "reply"    -> executeReply(email, decision);
            case "ignore"   -> executeIgnore(email);
            case "escalate" -> executeEscalate(email);
            case "notify"   -> executeNotify(email);
            case "label"    -> executeLabel(email);
            default         -> System.out.println("Unknown action: "
                    + decision.getAction() + " — ignoring");
        }
    }

    private void executeReply(EmailMessage email, DecisionResult decision) {
        String replyTo  = email.getReplyTo() != null
                ? email.getReplyTo() : email.getFrom();
        String template = decision.getReplyTemplate() != null
                ? decision.getReplyTemplate()
                : "Thank you for your email. We will get back to you shortly.";

        boolean sent = smtpService.sendEmail(
                replyTo,
                "Re: " + email.getSubject(),
                template
        );

        if (sent) System.out.println("✅ Reply sent to: " + replyTo);
        else      System.out.println("❌ Failed to send reply to: " + replyTo);
    }

    private void executeIgnore(EmailMessage email) {
        System.out.println("⏭️  Ignored: " + email.getSubject());
    }

    private void executeEscalate(EmailMessage email) {
        // For now — send escalation alert to yourself
        smtpService.sendEmail(
                email.getTo(),
                "🚨 ESCALATION: " + email.getSubject(),
                "This email requires human attention:\n\n"
                        + "From: " + email.getFrom() + "\n"
                        + "Subject: " + email.getSubject() + "\n"
                        + "Body: " + email.getBody()
        );
        System.out.println("🚨 Escalated: " + email.getSubject());
    }

    private void executeNotify(EmailMessage email) {
        smtpService.sendEmail(
                email.getTo(),
                "📬 Notification: " + email.getSubject(),
                "You received an email from: " + email.getFrom()
                        + "\nSubject: " + email.getSubject()
        );
        System.out.println("🔔 Notification sent for: " + email.getSubject());
    }

    private void executeLabel(EmailMessage email) {
        // Logging for now — full Gmail label via IMAP in Phase 6
        System.out.println("🏷️  Labeled: " + email.getSubject());
    }
}