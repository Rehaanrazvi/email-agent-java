package com.emailagent.model;



import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailMessage {

    private String messageId;
    private String subject;
    private String body;
    private String from;

    @Override
    public String toString() {
        return "\n--- Email ---" +
                "\nFrom    : " + from +
                "\nSubject : " + subject +
                "\nBody    : " + body.substring(0, Math.min(body.length(), 200)) +
                "\n-------------";
    }
}