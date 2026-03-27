# AI Email Agent 🤖📧

An AI-powered email automation system built with Java Spring Boot that reads incoming emails, analyzes them using rule-based and AI-driven decision making, and takes automated actions.

---

## 🚀 Project Status

| Phase | Description | Status |
|-------|-------------|--------|
| Phase 1 | Read Emails (IMAP) | ✅ Complete |
| Phase 2 | Send Emails (SMTP) | ✅ Complete |
| Phase 3 | Rule Engine (JSON-based) | ✅ Complete |
| Phase 4 | AI Integration (OpenAI) | 🔄 Up Next |
| Phase 5 | Agent Decision System | ⏳ Pending |
| Phase 6 | Logging + Dashboard | ⏳ Pending |

---

## 🧠 What It Does

- Connects to Gmail inbox via IMAP and fetches recent emails
- Extracts full email metadata — sender, subject, body, date, attachments, priority
- Runs emails through a JSON-based rule engine for instant decisions
- Falls back to OpenAI for emails that don't match any rule (Phase 4)
- Takes actions — reply, ignore, label, escalate, notify (Phase 5)
- Logs every decision with timestamp and confidence (Phase 6)

---

## 🏗️ System Architecture
```
Email Inbox
     ↓
EmailService (IMAP Fetch)
     ↓
RuleEngineService (JSON Rules)
     ↓ (no match)
AIService (OpenAI Classification) — Phase 4
     ↓
ActionService (Reply/Ignore/Label/Escalate) — Phase 5
     ↓
Logger + Dashboard — Phase 6
```

---

## 🛠️ Tech Stack

- **Java 17** + **Spring Boot 3.5.12**
- **Jakarta Mail** (angus-mail 2.0.4) — IMAP + SMTP
- **OpenAI API** — email classification (Phase 4)
- **Lombok** — boilerplate reduction
- **Jackson** — JSON parsing
- **Maven** — dependency management

---

## 📁 Project Structure
```
email-agent-java/
├── src/main/java/com/emailagent/
│   ├── EmailAgentJavaApplication.java
│   ├── model/
│   │   ├── EmailMessage.java
│   │   ├── Rule.java
│   │   └── DecisionResult.java
│   └── service/
│       ├── EmailService.java
│       ├── SmtpService.java
│       └── RuleEngineService.java
├── src/main/resources/
│   ├── application.properties        ← not committed (contains credentials)
│   ├── application.properties.example
│   └── rules.json
├── NOTES.md
└── pom.xml
```

---

## ⚙️ Setup & Run

### Prerequisites
- Java 17+
- Maven
- Gmail account with IMAP enabled + App Password

### 1. Clone the repo
```bash
git clone https://github.com/yourusername/email-agent-java.git
cd email-agent-java
```

### 2. Configure credentials
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
Fill in your Gmail and App Password in `application.properties`.

### 3. Run
```bash
mvn spring-boot:run
```

---

## 📋 How Rules Work

Rules are defined in `src/main/resources/rules.json`.
Each rule has conditions and an action:
```json
{
  "id": "rule_001",
  "name": "Job Application Reply",
  "conditions": {
    "subjectContains": ["job", "hiring", "recruitment"],
    "from": "",
    "hasAttachment": false,
    "priority": ""
  },
  "action": "reply",
  "replyTemplate": "Thank you for reaching out! I will get back to you shortly."
}
```

### Available Actions
| Action | Description |
|--------|-------------|
| `reply` | Send automated reply using replyTemplate |
| `ignore` | Do nothing, skip the email |
| `escalate` | Flag for human attention |
| `notify` | Send a notification |
| `label` | Categorize the email in Gmail |

---

## 🔮 Future Plans

- [ ] OpenAI integration for smart email classification
- [ ] Full agent decision system (rule → AI fallback)
- [ ] Action execution engine
- [ ] Logging with JSON storage → database
- [ ] Web dashboard UI
- [ ] Multi-user SaaS with Gmail OAuth
- [ ] Stripe payment integration

---

## 👨‍💻 Author

Built by **Rehaan** as a backend engineering project targeting SDE roles.

> "Not just a portfolio project — building toward a real product."
```

---

