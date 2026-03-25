package com.emailagent.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class EmailMessage {

    private String messageId;
    private String subject;
    private String body;
    private String from;
    private String to;
    private String replyTo;
    private String cc;
    private Date receivedDate;
    private boolean isRead;
    private boolean hasAttachment;
    private String priority;

    @Override
    public String toString() {
        return "\n--- Email ---" +
                "\nFrom        : " + from +
                "\nTo          : " + to +
                "\nSubject     : " + subject +
                "\nDate        : " + receivedDate +
                "\nRead        : " + isRead +
                "\nAttachment  : " + hasAttachment +
                "\nPriority    : " + priority +
                "\nBody        : " + body.substring(0, Math.min(body.length(), 200)) +
                "\n-------------";
    }
}