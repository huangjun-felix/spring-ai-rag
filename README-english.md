# Spring AI RAG Knowledge Base Q&A System

> A Retrieval-Augmented Generation (RAG) knowledge base Q&A system built with Spring Cloud microservices architecture and Spring AI

## 📖 Project Introduction

This project is a knowledge base Q&A system that supports PDF file upload, intelligent parsing, parent-child index vector storage, and AI-powered conversational retrieval. Users can upload PDF files, which are automatically processed through text extraction, OCR fallback, AI structural cleansing, vector database storage, and Elasticsearch keyword search. A chat interface enables intelligent Q&A about the uploaded document contents.

### Core Highlights

- 📄 **Intelligent PDF Parsing**: Native text extraction + OCR Chinese recognition fallback
- 🔍 **Three-Way Hybrid Search**: ES keyword matching + parent-child vector search + GraphRAG entity association
- 📊 **RRF Fusion Ranking**: Multi-path search result reciprocal rank fusion (k=60)
- 🎯 **LLM Reranker Refinement**: Top 20 documents re-scored by LLM, precise Top 5 selection
- 💬 **Skills Intent Routing**: LLM automatically identifies user intent, routes to different search strategies
- 🗂️ **Session Management**: Multi-session isolation, persistent chat history (MySQL + Redis)

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.5.13 | Base framework |
| Spring Cloud | 2025.0.2 | Microservices architecture |
| Spring Cloud Alibaba | 2025.0.0.0 | Nacos registry/config center |
| Spring AI | 1.1.4 | AI integration framework |
| OpenFeign | - | Inter-service remote calls |
| Spring Data Elasticsearch | 5.5.10 | ES search engine integration |
| Redis Vector Store | - | Vector database (1024 dimensions) |
| MyBatis-Plus | 3.5.13 | ORM framework |
| PDFBox | 2.0.30 | PDF text extraction |
| Tesseract | 5.11.0 | OCR Chinese recognition |
| Spring Cloud Gateway | WebFlux | API Gateway |

### AI Models
| Model | Purpose | Provider |
|-------|---------|----------|
| qwen3.6-plus | Dialogue generation, intent recognition, reranker scoring | Alibaba Qwen |
| text-embedding-v3 | Text embedding (1024 dimensions) | Alibaba Qwen |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Vue 3 | - | Component framework (Composition API) |
| Vite | - | Frontend build tool |
| Element Plus | - | UI component library |
| Tailwind CSS | v3 | Atomic CSS framework |
| marked | - | Markdown rendering |

---

## ✨ Features

### 1. PDF File Upload & Parsing
- Support PDF file upload (up to 100MB)
- Native text extraction → automatic OCR fallback on failure
- AI structural cleansing: module-based structured JSON output
- Parent-child index construction: parent block (full context 1000+ tokens) + child block (200-token high-precision snippets)

### 2. Knowledge Base Q&A
- User enters a question, triggering hybrid search automatically
- Three-way parallel search: ES BM25 + vector similarity + entity association
- RRF reciprocal rank fusion (k=60) → Top 20
- LLM Reranker refinement → Top 5
- RAG Synthesis Prompt generates answer (with citation markers `[1]` `[2]`)

### 3. Multi-Session Management
- Left sidebar session list, supports create, switch, delete
- Each session has independent message records, no cross-talk
- Sessions auto-created on first message, message history auto-loaded
- Upload success alerts are session-isolated

### 4. Session Memory & Persistence
- User messages and AI replies written to MySQL simultaneously
- Redis ChatMemory session cache (7-hour TTL)
- MySQL as Redis fallback storage, ensuring no message loss

---

## 📸 Screenshots

> Screenshot placeholders — replace with actual project screenshots

### Main Interface
![Main Interface](./docs/screenshot-main.png)

### File Upload
![File Upload](./docs/screenshot-upload.png)

### Multi-Session Management
![Session Management](./docs/screenshot-sessions.png)

### AI Q&A
![AI Q&A](./docs/screenshot-chat.png)

---

## 🌐 Online Demo

> **Demo URL**: [http://your-domain.com:3000](http://your-domain.com:3000)

| Access Method | Description |
|---------------|-------------|
| Web | Directly visit `http://localhost:3000` |
| Test Account | No login required, start using immediately |

---

## 🚀 Deployment

### Prerequisites

| Service | Address (Example) | Purpose |
|---------|-------------------|---------|
| Nacos | 192.168.1.137:8848 | Service registry & config center |
| MySQL | 47.100.32.35:3306 | Business data storage |
| Redis | 47.100.32.35:6379 | Vector storage + session cache |
| Elasticsearch | 192.168.1.137:9200 | Keyword search |
| MinIO | 192.168.1.137:9000 | File object storage |
| DashScope | https://dashscope.aliyuncs.com | Alibaba Qwen API |

### 1. Database Initialization

```sql
CREATE DATABASE IF NOT EXISTS springrag DEFAULT CHARSET utf8mb4;

-- Chat messages table
CREATE TABLE `t_chat_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message` varchar(255) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `sort` int(11) DEFAULT NULL,
  `session_id` varchar(64) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sessions table
CREATE TABLE `t_chat` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- File info table
CREATE TABLE `t_file_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) DEFAULT NULL,
  `session_id` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2. Configuration

Edit each module's `application.yaml` and replace the following:

```yaml
spring:
  datasource:
    url: jdbc:mysql://<your-mysql-ip>:3306/springrag
    username: root
    password: your_password
  data:
    redis:
      host: <your-redis-ip>
      port: 6379
  ai:
    openai:
      api-key: sk-your-dashscope-api-key
      chat:
        options:
          model: qwen3.6-plus
```

### 3. Start Backend Services

```bash
# Build the entire project
mvn clean package -DskipTests

# Start services in order (sequence matters)
java -jar rag-gateway/target/gateway-0.0.1-SNAPSHOT.jar     # Port 8080
java -jar rag-file/target/file-0.0.1-SNAPSHOT.jar          # Random port
java -jar rag-chat/target/chat-0.0.1-SNAPSHOT.jar          # Random port
```

### 4. Start Frontend

```bash
cd frontend
npm install
npm run dev
# → http://localhost:3000
```

### 5. Production Deployment

```bash
# Build frontend
cd frontend
npm run build

# Deploy dist directory to Nginx or other static server
# Nginx example
server {
    listen 80;
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    location /admin/ {
        proxy_pass http://localhost:8080;
    }
}
```

---

## 📁 Project Structure

```
spring-ai-rag/
├── pom.xml                          # Parent POM
├── frontend/                        # Vue3 frontend
├── rag-gateway/                     # Gateway module
├── rag-chat/                        # Chat / model module
├── rag-file/                        # File processing module
├── rag-common/                      # Common module
└── rag-feign/                       # Remote call module
```
