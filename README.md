# 🤖 AI Email Automation Agent (Java + Spring Boot)

An intelligent backend email automation system built using Java and Spring Boot that automatically fetches, analyzes, categorizes, and responds to emails using a hybrid Rule Engine + AI fallback architecture.

The system continuously monitors incoming emails, applies configurable automation rules, and leverages Grok AI when no rule matches are found.

---

# 🚀 Features

## 📥 Automated Email Fetching

* Fetches emails using IMAP
* Supports Gmail App Password authentication
* Continuously polls inbox using Spring Scheduler

## 🧠 Rule-Based Automation Engine

* Configurable rules using `rules.json`
* Detects keywords and triggers actions automatically
* Supports intelligent workflow processing

## 🤖 AI Fallback Decision System

* Uses Grok API when no matching rule exists
* AI suggests:

    * intent/category
    * recommended action
    * auto-generated reply

## 📤 Automated Email Replies

* Sends emails automatically using SMTP
* Supports custom response templates
* AI-generated fallback responses

## 🏷️ Email Categorization & Labeling

* Categorizes emails into:

    * JOB
    * SUPPORT
    * SPAM
    * IMPORTANT
    * MEETING
* Stores labels internally for tracking

## 🗄️ SQL Database Integration

* Stores processed emails
* Tracks:

    * sender
    * subject
    * category
    * action taken
    * timestamps

## ⏱️ Continuous Background Processing

* Uses Spring Scheduler
* Automatically processes emails at configurable intervals

---

# 🧠 System Architecture

```text
Incoming Email
       ↓
IMAP Fetch Service
       ↓
Email Parser
       ↓
Rule Engine (rules.json)
       ↓
Rule Found?
   YES → Execute Action
   NO  → Grok AI Analysis
               ↓
      AI Suggests Action
               ↓
SMTP Auto Reply
               ↓
Database Logging
```

---

# ⚙️ Tech Stack

| Technology       | Purpose                    |
| ---------------- | -------------------------- |
| Java             | Core backend language      |
| Spring Boot      | Backend framework          |
| Jakarta Mail     | IMAP & SMTP integration    |
| Grok API         | AI-powered decision making |
| MySQL / SQL      | Database storage           |
| Spring Scheduler | Background automation      |
| Maven            | Dependency management      |

---

# 📁 Project Structure

```text
email-agent-java/
│
├── model/
│   └── EmailMessage.java
│
├── service/
│   ├── EmailService.java
│   ├── SmtpService.java
│   ├── RuleEngineService.java
│   ├── GrokAIService.java
│   └── SchedulerService.java
│
├── resources/
│   ├── application.properties
│   └── rules.json
│
├── repository/
│
├── config/
│
└── EmailAgentJavaApplication.java
```

---

# ⚙️ How It Works

## 1️⃣ Fetch Emails

The application connects to the configured mailbox using IMAP and retrieves recent unread emails.

## 2️⃣ Parse Email Content

Extracts:

* Sender
* Subject
* Body

## 3️⃣ Rule Matching

The Rule Engine checks `rules.json` for matching keywords.

Example:

```json
{
  "keyword": "internship",
  "action": "reply",
  "label": "JOB"
}
```

## 4️⃣ AI Fallback

If no rule matches:

* Email is sent to Grok AI
* AI analyzes email intent
* AI suggests action/reply

## 5️⃣ Execute Action

Possible actions:

* reply
* label
* ignore
* notify

## 6️⃣ Store Logs

All processed email details are stored in the database.

---

# ⏱️ Automated Scheduling

The system automatically checks emails periodically using:

```java
@Scheduled(fixedDelayString = "${agent.poll.interval.ms}")
```

Example configuration:

```properties
agent.poll.interval.ms=60000
```

This checks emails every 60 seconds.

---

# 🔧 Installation

## Clone Repository

```bash
git clone https://github.com/Rehaanrazvi/email-agent-java.git
cd email-agent-java
```

---

# 📦 Install Dependencies

```bash
mvn clean install
```

---

# ⚙️ Configure Environment

Update `application.properties`:

```properties
# IMAP
mail.imap.host=imap.gmail.com
mail.imap.port=993
mail.imap.username=your-email@gmail.com
mail.imap.password=your-app-password

# SMTP
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.username=your-email@gmail.com
mail.smtp.password=your-app-password

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/email_agent
spring.datasource.username=root
spring.datasource.password=yourpassword

# Scheduler
agent.poll.interval.ms=60000

# Grok API
grok.api.key=your_api_key
```

---

# ▶️ Run Application

```bash
mvn spring-boot:run
```

---

# 📊 Example Workflow

## Incoming Email

Subject:

```text
Need internship opportunity
```

## Rule Match

```text
Keyword matched: internship
```

## Action Taken

```text
Action: AUTO_REPLY
Label: JOB
```

## Output

```text
Reply sent successfully
Stored in database
```

---

# 🧪 Current Capabilities

✅ IMAP Email Fetching
✅ SMTP Email Sending
✅ Rule-Based Decision Engine
✅ AI Fallback with Grok API
✅ SQL Database Integration
✅ Background Automation
✅ Email Categorization
✅ Configurable Polling

---

# 🚧 Future Enhancements

* REST APIs
* React Frontend Dashboard
* OAuth 2.0 Gmail Authentication
* Multi-user SaaS Support
* Gmail API Integration
* Analytics Dashboard
* AI Confidence Scoring
* Human Approval Workflow

---

# 🎯 Project Goal

The goal of this project is to build an intelligent workflow automation platform capable of autonomously processing and responding to emails using a hybrid architecture combining deterministic rules and AI-driven reasoning.

---

# 📚 Learning Outcomes

This project demonstrates:

* Backend System Design
* Email Protocol Integration
* Rule Engine Architecture
* AI-Assisted Decision Making
* Database Persistence
* Background Task Scheduling
* Automation Workflow Design

---

# 📄 License

MIT License

---

# 👨‍💻 Author

Developed by Rehaan Razvi

GitHub:
https://github.com/Rehaanrazvi
