package com.emailagent.service;

import com.emailagent.model.DecisionResult;
import com.emailagent.model.EmailMessage;
import com.emailagent.model.Rule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class RuleEngineService {

    private final List<Rule> rules = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Loads rules from rules.json when the service is created
    public RuleEngineService() {
        loadRules();
    }

    private void loadRules() {
        try {
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("rules.json");

            if (is == null) {
                System.err.println("rules.json not found!");
                return;
            }

            JsonNode root = objectMapper.readTree(is);
            JsonNode rulesNode = root.get("rules");

            for (JsonNode ruleNode : rulesNode) {
                Rule rule = objectMapper.treeToValue(ruleNode, Rule.class);
                rules.add(rule);
            }

            System.out.println("Loaded " + rules.size() + " rules from rules.json");

        } catch (Exception e) {
            System.err.println("Error loading rules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public DecisionResult evaluate(EmailMessage email) {
        for (Rule rule : rules) {
            if (matches(email, rule)) {
                return new DecisionResult(
                        email.getMessageId(),
                        rule.getId(),
                        rule.getName(),
                        rule.getAction(),
                        rule.getReplyTemplate(),
                        "RULE"
                );
            }
        }
        // No rule matched — return null, Phase 5 will send this to AI
        return null;
    }

    private boolean matches(EmailMessage email, Rule rule) {
        Rule.Conditions c = rule.getConditions();

        // Check subject keywords
        if (c.getSubjectContains() != null && !c.getSubjectContains().isEmpty()) {
            boolean keywordMatch = c.getSubjectContains().stream()
                    .anyMatch(keyword ->
                            email.getSubject() != null &&
                                    email.getSubject().toLowerCase()
                                            .contains(keyword.toLowerCase()));
            if (!keywordMatch) return false;
        }

        // Check sender
        if (c.getFrom() != null && !c.getFrom().isEmpty()) {
            if (email.getFrom() == null ||
                    !email.getFrom().toLowerCase()
                            .contains(c.getFrom().toLowerCase())) {
                return false;
            }
        }

        // Check attachment
        if (c.isHasAttachment() && !email.isHasAttachment()) {
            return false;
        }

        // Check priority
        if (c.getPriority() != null && !c.getPriority().isEmpty()) {
            if (!c.getPriority().equalsIgnoreCase(email.getPriority())) {
                return false;
            }
        }

        return true;
    }
}