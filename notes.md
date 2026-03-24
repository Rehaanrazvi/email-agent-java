# AI Email Agent — Learning Notes

## What is this project?
An AI-powered email automation system that reads incoming emails,
analyzes them using rules and AI, and takes actions like replying,
ignoring, labeling, or escalating.

---

## Tech Stack
- Java 17 + Spring Boot 3.5.12
- Jakarta Mail (angus-mail 2.0.4) — for IMAP/SMTP
- OpenAI API — for AI classification (Phase 4)
- Lombok — reduces boilerplate code
- Maven — dependency management

---

## Phase Roadmap
- [x] Phase 1 — Read Emails (IMAP)
- [ ] Phase 2 — Send Emails (SMTP)
- [ ] Phase 3 — Rule Engine (JSON-based)
- [ ] Phase 4 — AI Integration (OpenAI)
- [ ] Phase 5 — Agent Decision System
- [ ] Phase 6 — Logging + Dashboard

---

## Phase 1 — Read Emails (IMAP)

### What we did
Connected to Gmail inbox using IMAP protocol and fetched the last
10 emails, printing sender, subject, and body to the console.

### Why we did it
Before any AI or rules can work, the system needs to be able to
READ emails. This is the foundation everything else builds on.

### How it works — the flow
App starts → EmailService connects to Gmail IMAP server →
opens INBOX folder → loops through last 10 messages →
wraps each into EmailMessage object → prints to console

### Key files created
- `model/EmailMessage.java` — data container for one email
- `service/EmailService.java` — connects to Gmail and fetches emails
- `EmailAgentApplication.java` — entry point, triggers the fetch
- `application.properties` — Gmail credentials config (not committed)

### Concepts learned

**IMAP (Internet Message Access Protocol)**
- Protocol for READING emails from a mail server
- Emails stay on the server (vs POP3 which downloads and deletes)
- Gmail IMAP server: imap.gmail.com, port 993, SSL enabled
- We use READ_ONLY mode — safe, won't accidentally delete anything

**SMTP (Simple Mail Transfer Protocol)**
- Protocol for SENDING emails — we use this in Phase 2
- Gmail SMTP server: smtp.gmail.com, port 587

**Jakarta Mail (angus-mail)**
- Java library that implements IMAP and SMTP protocols
- Key classes we used:
    - `Session` — represents a mail session with config properties
    - `Store` — connection to the mail server
    - `Folder` — represents a mailbox folder like INBOX
    - `Message` — represents a single email

**App Password (Gmail)**
- Gmail blocks normal passwords for third-party apps
- App Password is a special 16-char password just for our app
- Required because we enabled 2-Step Verification
- NEVER commit this to Git — kept only in application.properties

**Spring Boot concepts used**
- `@SpringBootApplication` — marks the main entry point
- `@Service` — marks EmailService as a Spring-managed component
- `@Value("${property}")` — injects values from application.properties
- `CommandLineRunner` — runs code immediately when app starts

**Lombok annotations used**
- `@Getter` — auto-generates getters for all fields
- `@AllArgsConstructor` — auto-generates a constructor with all fields
- Saves us from writing 20+ lines of boilerplate code

### Why application.properties is gitignored
It contains your Gmail credentials. If pushed to GitHub, anyone
could read your emails. Always keep secrets out of version control.
In real projects, secrets are stored in environment variables or
secret managers like AWS Secrets Manager or HashiCorp Vault.

---

## Commit History
- `feat: Phase 1 - project setup and IMAP email fetching`

### Phase 1 Result
✅ Successfully fetched 10 real emails from Gmail inbox
- Sender, subject extracted correctly for all emails
- Body empty for HTML-only emails (to be improved later)
- Body shows raw HTML for rich emails (to be cleaned later)
```

**2. Commit and push:**
```
feat: Phase 1 complete - IMAP email fetching working




## Phase 2 — Send Emails (SMTP)

### What we did
Added the ability to send emails using Gmail's SMTP server.
Tested by sending an email to ourselves and confirming it arrived.

### Why we did it
The agent needs to be able to REPLY to emails. Without SMTP,
it can only read — it has no voice. This is the foundation
for auto-replies in Phase 5.

### How it works — the flow
SmtpService gets (to, subject, body) →
creates SMTP session with Gmail credentials →
builds a MimeMessage → Transport.send() delivers it

### Key files created
- `service/SmtpService.java` — connects to Gmail SMTP and sends emails

### Concepts learned

**SMTP (Simple Mail Transfer Protocol)**
- Protocol specifically for SENDING emails
- Gmail SMTP server: smtp.gmail.com, port 587
- Port 587 uses STARTTLS — starts as plain connection then
  upgrades to encrypted. More firewall-friendly than port 465.

**STARTTLS vs SSL**
- IMAP used port 993 with SSL (encrypted from the start)
- SMTP uses port 587 with STARTTLS (upgrades to encrypted)
- Both are secure — just different handshake approaches

**Jakarta Mail classes used**
- `Session.getInstance(props, Authenticator)` — session with auth
- `Authenticator` — anonymous class that provides credentials
- `PasswordAuthentication` — wraps username + password
- `MimeMessage` — the actual email object being constructed
- `InternetAddress` — parses and validates email addresses
- `Transport.send()` — static method that delivers the message

**Why same App Password works for both IMAP and SMTP**
- The App Password is tied to your Google account, not a protocol
- Same 16-char password works for both reading and sending
- In application.properties we just reference it twice

**Spring concept used**
- `@Service` — SmtpService is a Spring managed bean just like
  EmailService, meaning Spring creates it and injects it
  wherever needed via dependency injection

### Phase 2 Result
✅ Successfully sent a test email to self via Gmail SMTP
- Email arrived in inbox within seconds
- Subject and body transmitted correctly
```

---

Add this to `NOTES.md`, then commit everything:
```
feat: Phase 2 complete - SMTP email sending working