package com.emailagent.service;

import com.emailagent.model.EmailMessage;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${mail.imap.host}")
    private String host;

    @Value("${mail.imap.port}")
    private int port;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    public List<EmailMessage> fetchUnreadEmails() {
        List<EmailMessage> emails = new ArrayList<>();

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", String.valueOf(port));
        props.put("mail.imaps.ssl.enable", "true");

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            int total = messages.length;
            int start = Math.max(0, total - 10); // last 10 emails

            for (int i = start; i < total; i++) {
                Message msg = messages[i];
                String subject = msg.getSubject() != null ? msg.getSubject() : "(no subject)";
                String from    = msg.getFrom() != null ? msg.getFrom()[0].toString() : "(unknown)";
                String body    = extractBody(msg);
                String id      = String.valueOf(msg.getMessageNumber());

                emails.add(new EmailMessage(id, subject, body, from));
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            System.err.println("Error fetching emails: " + e.getMessage());
            e.printStackTrace();
        }

        return emails;
    }

    private String extractBody(Message message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof MimeMultipart multipart) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                if (part.getContentType().startsWith("text/plain")) {
                    sb.append(part.getContent().toString());
                }
            }
            return sb.toString();
        }
        return "(unable to extract body)";
    }
}