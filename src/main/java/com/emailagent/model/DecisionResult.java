package com.emailagent.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DecisionResult {

    private String emailId;
    private String matchedRuleId;
    private String matchedRuleName;
    private String action;
    private String replyTemplate;
    private String decisionSource; // "RULE" or "AI" (used in Phase 5)

    @Override
    public String toString() {
        return "\n--- Decision ---" +
                "\nEmail ID     : " + emailId +
                "\nMatched Rule : " + matchedRuleName +
                "\nAction       : " + action +
                "\nSource       : " + decisionSource +
                "\n----------------";
    }
}