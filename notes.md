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
- [✅] Phase 1 — Read Emails (IMAP)

- [✅] Phase 2 — Send Emails (SMTP)
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
Update — Enriched EmailMessage model
Added more fields to EmailMessage to capture full email metadata:
- `to` — recipient address
- `replyTo` — where replies should go
- `cc` — carbon copy
- `receivedDate` — when email arrived
- `isRead` — read/unread status
- `hasAttachment` — whether files are attached
- `priority` — extracted from X-Priority header (HIGH/NORMAL/LOW)

These fields are essential for the Rule Engine in Phase 3 —
rules can now be based on sender, read status, attachments,
priority and more — not just subject keywords.




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


## Phase 3 — Rule Engine

### What we did
Built a JSON-based rule engine that loads rules from rules.json
and matches them against incoming emails to decide what action
to take.

### Why we did it
Not every email needs AI — simple patterns like spam keywords
or known senders can be handled instantly with rules. Rules are
faster, cheaper, and more predictable than AI. AI is the
fallback for emails rules can't handle (Phase 5).

### How it works — the flow
RuleEngineService loads rules.json on startup →
for each email, loops through all rules →
checks conditions (keywords, sender, attachment, priority) →
first matching rule wins → returns DecisionResult →
if no rule matches → returns null (goes to AI in Phase 5)

### Key files created
- `resources/rules.json` — rule definitions (editable without recompiling)
- `model/Rule.java` — Java object representing one rule
- `model/DecisionResult.java` — result object: action + source + template
- `service/RuleEngineService.java` — loads and evaluates rules

### Concepts learned

**Why JSON for rules?**
Rules are stored in JSON not Java code — this means you can
add, edit, or remove rules without recompiling the app. In the
SaaS version, users will be able to define their own rules
through the dashboard and the JSON updates dynamically.

**Rule matching logic**
- Conditions use AND logic — ALL non-empty conditions must match
- Keywords use OR logic — ANY keyword in the list triggers a match
- First matching rule wins — order matters in rules.json
- Empty string conditions are skipped (wildcard)

**DecisionResult — the Decision Object**
Defined in our documentation as having: intent, action, confidence.
We track decisionSource ("RULE" or "AI") so Phase 5 knows
whether the decision came from a rule or from OpenAI.

**Actions defined so far**
- `reply` — send an automated reply using replyTemplate
- `ignore` — do nothing, skip the email
- `escalate` — flag for human attention
- `notify` — send a notification
- `label` — categorize the email

**ObjectMapper (Jackson)**
- Jackson is the JSON library bundled with Spring Boot
- `objectMapper.readTree()` — parses JSON into a tree structure
- `objectMapper.treeToValue()` — converts JSON node into a Java object
- We already had Jackson from spring-boot-starter-web — no new dependency needed

### Phase 3 Result
✅ Rule engine loads 4+ rules from rules.json on startup
✅ Evaluates all fetched emails against rules
✅ Returns DecisionResult with action and source for matches
✅ Returns null for unmatched emails (to be handled by AI in Phase 5)
```

