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