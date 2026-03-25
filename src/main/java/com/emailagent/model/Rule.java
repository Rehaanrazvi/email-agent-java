package com.emailagent.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Rule {

    private String id;
    private String name;
    private Conditions conditions;
    private String action;
    private String replyTemplate;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Conditions {
        private List<String> subjectContains;
        private String from;
        private boolean hasAttachment;
        private String priority;
    }
}