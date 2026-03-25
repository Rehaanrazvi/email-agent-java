package com.emailagent.service;

import com.emailagent.model.EmailMessage;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    // How many days back to fetch — change this anytime
    private static final int FETCH_DAYS_BACK = 7;
    // How many emails max to process
    private static final int MAX_EMAILS = 10;

    public List<EmailMessage> fetchUnreadEmails() {
        List<EmailMessage> emails = new ArrayList<>();
        List<Folder> openFolders = new ArrayList<>();

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", String.valueOf(port));
        props.put("mail.imaps.ssl.enable", "true");

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            // Date filter — only fetch emails from last X days
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -FETCH_DAYS_BACK);
            Date since = cal.getTime();
            SearchTerm dateTerm = new ReceivedDateTerm(ComparisonTerm.GE, since);

            System.out.println("Fetching emails from last " + FETCH_DAYS_BACK + " days...");

            // Collect messages from all folders using date filter
            List<Message> allMessages = new ArrayList<>();

            String[] folderNames = {
                    "INBOX",
                    "[Gmail]/Promotions",
                    "[Gmail]/Social",
                    "[Gmail]/Updates"
            };

            for (String folderName : folderNames) {
                try {
                    Folder folder = store.getFolder(folderName);
                    if (folder.exists()) {
                        folder.open(Folder.READ_ONLY);
                        openFolders.add(folder);

                        // Search with date filter — only recent emails
                        Message[] messages = folder.search(dateTerm);
                        allMessages.addAll(List.of(messages));
                        System.out.println("Found " + messages.length
                                + " recent emails in: " + folderName);
                    }
                } catch (Exception e) {
                    System.out.println("Skipping folder: "
                            + folderName + " — " + e.getMessage());
                }
            }

            // Sort by date — newest first
            allMessages.sort((a, b) -> {
                try {
                    Date dateA = a.getReceivedDate() != null
                            ? a.getReceivedDate() : a.getSentDate();
                    Date dateB = b.getReceivedDate() != null
                            ? b.getReceivedDate() : b.getSentDate();
                    if (dateA == null || dateB == null) return 0;
                    return dateB.compareTo(dateA);
                } catch (Exception e) {
                    return 0;
                }
            });

            // Take top MAX_EMAILS
            List<Message> topEmails = allMessages.subList(
                    0, Math.min(MAX_EMAILS, allMessages.size()));

            System.out.println("Processing top " + topEmails.size() + " emails...\n");

            // Process each message
            for (Message msg : topEmails) {
                try {
                    String from = msg.getFrom() != null
                            ? msg.getFrom()[0].toString() : "(unknown)";

                    String to = msg.getRecipients(Message.RecipientType.TO) != null
                            ? msg.getRecipients(Message.RecipientType.TO)[0].toString()
                            : "(unknown)";

                    String subject = msg.getSubject() != null
                            ? msg.getSubject() : "(no subject)";

                    String replyTo = msg.getReplyTo() != null
                            && msg.getReplyTo().length > 0
                            ? msg.getReplyTo()[0].toString() : from;

                    String cc = "";
                    if (msg.getRecipients(Message.RecipientType.CC) != null) {
                        cc = msg.getRecipients(
                                Message.RecipientType.CC)[0].toString();
                    }

                    Date receivedDate = msg.getReceivedDate() != null
                            ? msg.getReceivedDate() : msg.getSentDate();

                    boolean isRead = msg.isSet(Flags.Flag.SEEN);
                    boolean hasAttachment = hasAttachment(msg);
                    String priority = extractPriority(msg);
                    String body = extractBody(msg);
                    String id = String.valueOf(msg.getMessageNumber());

                    emails.add(new EmailMessage(
                            id, subject, body, from, to,
                            replyTo, cc, receivedDate,
                            isRead, hasAttachment, priority
                    ));

                } catch (Exception e) {
                    System.out.println("Skipping one email: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching emails: " + e.getMessage());
            e.printStackTrace();

        } finally {
            for (Folder folder : openFolders) {
                try {
                    if (folder.isOpen()) folder.close(false);
                } catch (Exception ignored) {}
            }
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
            return sb.isEmpty() ? "(html only email)" : sb.toString();
        }
        return "(unable to extract body)";
    }

    private boolean hasAttachment(Message message) throws Exception {
        if (message.getContentType().startsWith("multipart")) {
            MimeMultipart multipart = (MimeMultipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String extractPriority(Message message) throws Exception {
        String[] headers = message.getHeader("X-Priority");
        if (headers != null && headers.length > 0) {
            return switch (headers[0].trim()) {
                case "1" -> "HIGH";
                case "3" -> "NORMAL";
                case "5" -> "LOW";
                default  -> "NORMAL";
            };
        }
        return "NORMAL";
    }
}