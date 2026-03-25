# AI Email Agent — Learning Notes

## What is this project?
An AI-powered email automation system that reads incoming emails,
analyzes them using rules and AI, and takes actions like replying,
ignoring, labeling, or escalating.

## Big Picture Vision
Eventually a SaaS product where users sign up, connect their Gmail,
set their own rules, and let the AI agent handle their inbox.
Core engine is being built now — web layer comes after Phase 6.

---

## Tech Stack
- Java 17 + Spring Boot 3.5.12
- Jakarta Mail (angus-mail 2.0.4) — for IMAP/SMTP
- OpenAI API — for AI classification (Phase 4)
- Lombok — reduces boilerplate code
- Jackson — JSON parsing (bundled with Spring Boot)
- Maven — dependency management

---

## Phase Roadmap
- [x] Phase 1 — Read Emails (IMAP) ✅
- [x] Phase 2 — Send Emails (SMTP) ✅
- [x] Phase 3 — Rule Engine (JSON-based) ✅
- [ ] Phase 4 — AI Integration (OpenAI)
- [ ] Phase 5 — Agent Decision System
- [ ] Phase 6 — Logging + Dashboard

---

## Phase 1 — Read Emails (IMAP)

### What we did
Connected to Gmail inbox using IMAP protocol and fetched the last
10 emails, printing sender, subject, body and metadata to console.

### Why we did it
Before any AI or rules can work, the system needs to be able to
READ emails. This is the foundation everything else builds on.

### How it works — the flow
App starts → EmailService connects to Gmail IMAP server →
searches folders with date filter (last 7 days) →
sorts by newest first → takes top 10 →
wraps each into EmailMessage object → prints to console

### Key files created
- `model/EmailMessage.java` — data container for one email
- `service/EmailService.java` — connects to Gmail and fetches emails
- `EmailAgentApplication.java` — entry point, triggers the fetch
- `application.properties` — Gmail credentials config (not committed)
- `application.properties.example` — safe template for GitHub

### EmailMessage fields
- `messageId` — unique ID of the email
- `subject` — subject line
- `body` — plain text content
- `from` — sender address
- `to` — recipient address
- `replyTo` — where replies should go (sometimes differs from from)
- `cc` — carbon copy recipients
- `receivedDate` — when the email arrived
- `isRead` — has it been read?
- `hasAttachment` — does it have files attached?
- `priority` — extracted from X-Priority header (HIGH/NORMAL/LOW)

### Concepts learned

**IMAP (Internet Message Access Protocol)**
- Protocol for READING emails from a mail server
- Emails stay on the server (vs POP3 which downloads and deletes)
- Gmail IMAP server: imap.gmail.com, port 993, SSL enabled
- We use READ_ONLY mode — safe, won't accidentally delete anything

**Gmail folders via IMAP**
- Gmail auto-sorts emails into tabs — INBOX, Promotions, Social, Updates
- In IMAP these are separate folders: [Gmail]/Promotions etc
- We fetch from all 4 folders and merge + sort them
- Labels in Gmail are also exposed as IMAP folders

**Server-side search with ReceivedDateTerm**
- Instead of downloading ALL emails and filtering in Java,
  we send a date filter to Gmail's server directly
- Only emails newer than X days are returned
- Much faster — avoids loading thousands of old emails
- FETCH_DAYS_BACK = 7 and MAX_EMAILS = 10 are easy to change

**Jakarta Mail key classes**
- `Session` — mail session with config properties
- `Store` — connection to the mail server
- `Folder` — represents a mailbox folder like INBOX
- `Message` — represents a single email
- `SearchTerm` — server-side filter (date, sender, subject etc)

**FolderClosedException — what we learned**
- Message objects in Jakarta Mail are lazy loaded
- They need the folder connection open to read data
- Fix: keep all folders open while processing, close in finally block
- `finally` block always runs — perfect for cleanup/closing resources

**App Password (Gmail)**
- Gmail blocks normal passwords for third-party apps
- App Password is a special 16-char password just for our app
- Required because we enabled 2-Step Verification
- NEVER commit this to Git — kept only in application.properties

**Spring Boot concepts used**
- `@SpringBootApplication` — marks the main entry point
- `@Service` — marks a class as a Spring-managed component
- `@Value("${property}")` — injects values from application.properties
- `CommandLineRunner` — runs code immediately when app starts

**Lombok annotations used**
- `@Getter` — auto-generates getters for all fields
- `@AllArgsConstructor` — auto-generates constructor with all fields
- `@NoArgsConstructor` — auto-generates empty constructor
- `@Setter` — auto-generates setters
- Saves writing 20+ lines of boilerplate code

### Phase 1 Result
✅ Successfully fetched 10 real emails from Gmail inbox
✅ Fetches from INBOX, Promotions, Social, Updates folders
✅ Sorted by newest first using server-side date filter
✅ Full metadata extracted per email

---

## Phase 2 — Send Emails (SMTP)

### What we did
Added ability to send emails using Gmail's SMTP server.
Tested by sending an email to ourselves and confirming it arrived.

### Why we did it
The agent needs to be able to REPLY to emails. Without SMTP
it can only read — it has no voice. Foundation for auto-replies
in Phase 5.

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
- Port 587 uses STARTTLS — starts plain then upgrades to encrypted

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
- App Password is tied to your Google account, not a protocol
- Same 16-char password works for both reading and sending

**Spring concept**
- Both EmailService and SmtpService are `@Service` beans
- Spring creates them and injects wherever needed automatically

### Phase 2 Result
✅ Successfully sent a test email to self via Gmail SMTP
✅ Email arrived in inbox within seconds

---

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
- `resources/rules.json` — rule definitions (editable anytime)
- `model/Rule.java` — Java object representing one rule
- `model/DecisionResult.java` — result: action + source + template
- `service/RuleEngineService.java` — loads and evaluates rules

### Concepts learned

**Why JSON for rules?**
Rules are in JSON not Java code — add, edit, or remove rules
without recompiling. In SaaS version, users define their own
rules through the dashboard and JSON updates dynamically.

**Rule matching logic**
- Conditions use AND logic — ALL non-empty conditions must match
- Keywords use OR logic — ANY keyword in the list triggers match
- First matching rule wins — order matters in rules.json
- Empty string conditions are skipped (acts as wildcard)

**DecisionResult — the Decision Object**
Tracks: emailId, matchedRuleId, matchedRuleName, action,
replyTemplate, decisionSource ("RULE" or "AI").
decisionSource tells Phase 5 where the decision came from.

**Actions defined**
- `reply` — send automated reply using replyTemplate
- `ignore` — do nothing, skip the email
- `escalate` — flag for human attention
- `notify` — send a notification
- `label` — categorize the email (Gmail label via IMAP in Phase 5)

**Gmail Labels via IMAP**
Labels in Gmail are not folders — they are tags on emails.
But IMAP exposes them as folders. Our label action will copy
the email to the label folder so it appears tagged in Gmail.

**ObjectMapper (Jackson)**
- Jackson is the JSON library bundled with Spring Boot
- `objectMapper.readTree()` — parses JSON into a tree
- `objectMapper.treeToValue()` — converts JSON node to Java object
- No new dependency needed — came with spring-boot-starter-web

### Phase 3 Result
✅ Rule engine loads rules from rules.json on startup
✅ Evaluates all fetched emails against all rules
✅ Returns DecisionResult with action and source for matches
✅ Returns null for unmatched emails (AI handles in Phase 5)

---

## Commit History
- `feat: Phase 1 - project setup and IMAP email fetching`
- `feat: Phase 1 complete - IMAP email fetching working`
- `refactor: enrich EmailMessage model with full email metadata`
- `feat: Phase 2 complete - SMTP email sending working`
- `feat: Phase 3 complete - JSON rule engine working`
- `fix: FolderClosedException - keep folders open during processing`
- `perf: server-side date filter to avoid loading all emails`
```


