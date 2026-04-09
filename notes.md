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


## Phase 4 — AI Integration (Groq/Llama)

### What we did
Integrated Groq's free LLM API (Llama 3.1) to classify emails
that don't match any rule. AI returns intent, action and confidence.

### Why we did it
Rules can't handle every email — unknown senders, unusual subjects,
context-dependent emails need intelligence. AI fills that gap.

### How it works — the flow
Email fails rule matching → AIService sends prompt to Groq API →
Llama classifies email → returns intent + action + confidence →
wrapped into DecisionResult with source "AI"

### Key files
- `service/AIService.java` — calls Groq API and parses response

### Concepts learned

**Why Groq over OpenAI/Gemini?**
- OpenAI requires paid credits
- Gemini free tier had limit:0 for this Google account/region
- Groq is completely free, no card needed, very fast
- Uses open source Llama model — same quality for classification

**Prompt engineering**
- We tell the AI exactly what format to respond in
- Strict JSON only — no extra text
- Define allowed actions explicitly in the prompt
- System message sets the AI's role and constraints

**ObjectMapper for request building**
- Never build JSON by hand with string concatenation
- Special characters (emojis, Hindi, quotes) break manual JSON
- ObjectMapper.createObjectNode() builds valid JSON safely always

**Jackson JSON parsing**
- `root.path("choices").get(0).path("message").path("content")`
- `.path()` is null-safe, `.get()` can return null — use carefully
- Strip ```json fences — AI sometimes wraps response in markdown

### Phase 4 Result
✅ Groq API connected and working
✅ Llama 3.1 classifying emails intelligently
✅ Rule engine + AI fallback working together as pipeline
✅ DecisionResult correctly shows source as "AI" vs "RULE"

## Phase 5 — Agent Decision System

### What we did
Built the ActionService that actually EXECUTES decisions made by
the rule engine and AI. The agent now reads, decides, AND acts.

### Why we did it
Phases 1-4 were all thinking — no doing. Phase 5 gives the agent
hands. It can now reply, ignore, escalate, notify and label emails
automatically without human intervention.

### How it works — the flow
DecisionResult → ActionService.execute() →
switch on action type → call appropriate method →
reply via SmtpService / ignore / escalate / notify / label

### Key files created
- `service/ActionService.java` — executes decisions

### Actions implemented
- `reply` — sends automated reply using replyTemplate from rules
  or default template if AI decision
- `ignore` — skips the email, logs it
- `escalate` — forwards email to owner with ESCALATION subject
- `notify` — sends notification email to owner
- `label` — placeholder for now, real IMAP labeling in Phase 6

### Concepts learned

**Switch expressions (Java 14+)**
- `case "reply" -> executeReply()` — arrow syntax, no fall-through
- Cleaner than traditional switch with break statements
- Each case calls a dedicated private method — single responsibility

**Constructor injection vs @Autowired**
- We inject SmtpService via constructor not @Autowired field
- Constructor injection is preferred — makes dependencies explicit,
  easier to test, works with final fields
- Spring automatically injects when there's one constructor

**replyTo vs from**
- Emails have a `Reply-To` header separate from `From`
- Newsletters often have From: newsletter@company.com but
  Reply-To: no-reply@company.com
- Always reply to `replyTo` first, fall back to `from`

### Phase 5 Result
✅ ActionService executing all 5 action types
✅ Real emails being replied to via SMTP
✅ Full pipeline working: Fetch → Rule/AI → Execute

---

## Phase 6 — Logging + Spring Scheduler

### What we did
Added MySQL database logging for every decision, Spring Scheduler
for automatic runs every 2 minutes, and memory so the agent never
processes the same email twice.

### Why we did it
Without logging the agent has no memory — it processes the same
emails every run. Without scheduler it needs manual starts.
Phase 6 makes it a true autonomous background agent.

### How it works — the flow
App starts once → Scheduler triggers every 2 minutes →
fetch emails → check MySQL for already processed IDs →
skip known emails → process new ones → log to MySQL → repeat

### Key files created
- `model/ProcessedEmail.java` — JPA entity, maps to MySQL table
- `repository/ProcessedEmailRepository.java` — database queries
- `service/LoggerService.java` — checks + saves processed emails
- `service/AgentService.java` — main scheduled agent orchestrator

### Files updated
- `EmailAgentApplication.java` — added @EnableScheduling,
  removed CommandLineRunner (AgentService replaces it)
- `application.properties` — MySQL config + scheduler interval
- `pom.xml` — added spring-boot-starter-data-jpa + mysql-connector

### Concepts learned

**Spring Scheduler**
- `@EnableScheduling` on main class enables the scheduler
- `@Scheduled(fixedDelayString = "${agent.poll.interval.ms}")`
  runs a method repeatedly with a fixed delay between runs
- `fixedDelay` = waits X ms AFTER previous run completes
- `fixedRate` = runs every X ms regardless of previous run time
- We use fixedDelay — safer, prevents overlapping runs

**JPA and Spring Data**
- JPA (Jakarta Persistence API) — standard for ORM in Java
- Hibernate is the JPA implementation Spring Boot uses
- `@Entity` — marks class as a database table
- `@Table(name="processed_emails")` — sets table name
- `@Id` — marks primary key field
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` — auto increment
- Spring auto-creates the table on first run with `ddl-auto=update`

**Spring Data JPA Repository**
- Extend `JpaRepository<Entity, IdType>` — get CRUD for free
- Method name conventions generate SQL automatically:
    - `existsByUserIdAndEmailId()` → SELECT EXISTS WHERE userId=? AND emailId=?
    - `findByUserIdOrderByProcessedAtDesc()` → SELECT * WHERE userId=? ORDER BY processedAt DESC
    - `countByUserIdAndAction()` → SELECT COUNT(*) WHERE userId=? AND action=?
- No SQL written — Spring generates it from method names

**HikariCP Connection Pool**
- HikariCP is Spring Boot's default connection pool
- Instead of opening a new DB connection per request (slow),
  it maintains a pool of ready connections
- Automatically included with spring-boot-starter-data-jpa

**userId field — SaaS-ready design**
- Every log entry has a `userId` field — currently "default_user"
- When SaaS is built, each user's emails are isolated by userId
- Zero code changes needed — just pass real userId instead of constant
- This is forward-compatible design

**Why MySQL over JSON file**
- JSON file breaks under concurrent access (multiple users)
- MySQL handles concurrent reads/writes safely
- Scales to millions of records with indexes
- Same JPA code works with PostgreSQL — just change config
- Real querying — filter by date, action, source, confidence

**fixedDelay vs fixedRate**
- `fixedDelay=120000` — waits 2 min AFTER run completes
  If run takes 30s → next run starts at 2m30s
- `fixedRate=120000` — runs every 2 min from start
  If run takes 30s → next run starts at 2m exactly
  Risk: runs can overlap if processing is slow

**spring.jpa.open-in-view=false**
- By default Spring keeps DB connection open during view rendering
- We disabled it — best practice, prevents connection pool exhaustion
- Important for SaaS with many concurrent users

### Phase 6 Result
✅ MySQL connected, table auto-created
✅ Every decision logged with timestamp, action, source
✅ Memory working — already processed emails skipped on next run
✅ Agent runs automatically every 2 minutes via Spring Scheduler
✅ Stats printed after every run
✅ SaaS-ready with userId field for multi-user support
✅ Tomcat running on port 8080 — ready for REST API layer

---

## Commit History
- `feat: Phase 1 - project setup and IMAP email fetching`
- `feat: Phase 1 complete - IMAP email fetching working`
- `refactor: enrich EmailMessage model with full email metadata`
- `feat: Phase 2 complete - SMTP email sending working`
- `feat: Phase 3 complete - JSON rule engine working`
- `fix: FolderClosedException - keep folders open during processing`
- `perf: server-side date filter to avoid loading all emails`
- `feat: Phase 4 complete - AI email classification with Groq/Llama`
- `feat: Phase 5 complete - agent decision system executing actions`
- `feat: Phase 6 complete - MySQL logging + Spring Scheduler auto-run`


