# Spring AI RAG 知识库智能问答系统

> 基于 Spring Cloud 微服务架构 + Spring AI 技术构建的检索增强生成（RAG）知识库问答系统

## 📖 项目介绍

本项目是一个支持 PDF 文件上传、智能解析、父子索引向量化存储以及 AI 对话检索的知识库问答系统。系统支持上传 PDF 文件，自动完成文本提取、OCR 兜底识别、AI 结构化清洗、向量数据库存储以及 Elasticsearch 关键词检索，用户可通过聊天界面针对上传文件内容进行智能问答。

### 核心特性

- 📄 **PDF 智能解析**：原生文本提取 + OCR 中文识别兜底
- 🔍 **三路混合检索**：ES 关键词匹配 + 父子索引向量检索 + GraphRAG 实体联想
- 📊 **RRF 融合排序**：多路检索结果倒数排名融合（k=60）
- 🎯 **LLM Reranker 精排**：Top 20 文档二次 LLM 打分，精准取 Top 5
- 💬 **Skills 意图分发**：LLM 自动识别用户意图，路由到不同检索策略
- 🗂️ **会话管理**：多会话隔离，聊天记录持久化（MySQL + Redis）

---

## 🛠 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.13 | 基础框架 |
| Spring Cloud | 2025.0.2 | 微服务架构 |
| Spring Cloud Alibaba | 2025.0.0.0 | Nacos 注册/配置中心 |
| Spring AI | 1.1.4 | AI 集成框架 |
| OpenFeign | - | 微服务间远程调用 |
| Spring Data Elasticsearch | 5.5.10 | ES 搜索引擎集成 |
| Redis Vector Store | - | 向量数据库（1024 维） |
| MyBatis-Plus | 3.5.13 | ORM 框架 |
| PDFBox | 2.0.30 | PDF 文本提取 |
| Tesseract | 5.11.0 | OCR 中文识别 |
| Spring Cloud Gateway | WebFlux | API 网关 |

### AI 模型
| 模型 | 用途 | 提供商 |
|------|------|--------|
| qwen3.6-plus | 对话生成、意图识别、Reranker 打分 | 阿里通义千问 |
| text-embedding-v3 | 文本向量化（1024 维） | 阿里通义千问 |

### 前端
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | - | 组件框架（Composition API） |
| Vite | - | 前端构建工具 |
| Element Plus | - | UI 组件库 |
| Tailwind CSS | v3 | 原子化样式 |
| marked | - | Markdown 渲染 |

---

## ✨ 功能介绍

### 1. PDF 文件上传与解析
- 支持 PDF 文件上传（最大 100MB）
- 原生文本提取 → 失败自动降级为 OCR 识别
- AI 结构化清洗：按模块分类输出结构化 JSON
- 父子索引构建：父块（完整上下文 1000+ token）+ 子块（200 token 高精度片段）

### 2. 知识库智能问答
- 用户输入问题，自动触发混合检索
- 三路并行检索：ES BM25 + 向量相似度 + 实体联想
- RRF 倒数排名融合（k=60）→ Top 20
- LLM Reranker 精排 → Top 5
- RAG Synthesis Prompt 生成回答（带引用标注 `[1]` `[2]`）

### 3. 多会话管理
- 左侧会话列表，支持新建、切换、删除
- 每个会话独立消息记录，互不干扰
- 会话首次对话自动创建，消息历史自动加载
- 上传成功提示按会话隔离

### 4. 会话记忆与持久化
- 用户消息和 AI 回复同步写入 MySQL
- Redis ChatMemory 会话缓存（7 小时 TTL）
- MySQL 作为 Redis 回源存储，确保消息不丢失

---

## 📸 页面截图

> 截图占位，请替换为实际项目截图

### 主界面
![主界面](./docs/screenshot-main.png)

### 上传文件
![上传文件](./docs/screenshot-upload.png)

### 多会话管理
![会话管理](./docs/screenshot-sessions.png)

### AI 问答
![AI 问答](./docs/screenshot-chat.png)

---

## 🌐 在线 Demo

> **Demo 地址**：[http://your-domain.com:3000](http://your-domain.com:3000)

| 访问方式 | 说明 |
|---------|------|
| Web | 直接访问前端地址 `http://localhost:3000` |
| 测试账号 | 无需登录，直接开始使用 |

---

## 🚀 部署方式

### 环境要求

| 服务 | 地址（示例） | 用途 |
|------|-------------|------|
| Nacos | 192.168.1.137:8848 | 服务注册与配置中心 |
| MySQL | 47.100.32.35:3306 | 业务数据存储 |
| Redis | 47.100.32.35:6379 | 向量存储 + 会话缓存 |
| Elasticsearch | 192.168.1.137:9200 | 关键词检索 |
| MinIO | 192.168.1.137:9000 | 文件对象存储 |
| DashScope | https://dashscope.aliyuncs.com | 通义千问 API |

### 1. 数据库初始化

```sql
CREATE DATABASE IF NOT EXISTS springrag DEFAULT CHARSET utf8mb4;

-- 聊天记录表
CREATE TABLE `t_chat_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message` varchar(255) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `sort` int(11) DEFAULT NULL,
  `session_id` varchar(64) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 会话表
CREATE TABLE `t_chat` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文件信息表
CREATE TABLE `t_file_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) DEFAULT NULL,
  `session_id` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2. 配置修改

编辑各模块 `application.yaml`，替换以下配置：

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

### 3. 启动后端服务

```bash
# 编译整个项目
mvn clean package -DskipTests

# 依次启动（顺序不可颠倒）
java -jar rag-gateway/target/gateway-0.0.1-SNAPSHOT.jar     # 端口 8080
java -jar rag-file/target/file-0.0.1-SNAPSHOT.jar          # 随机端口
java -jar rag-chat/target/chat-0.0.1-SNAPSHOT.jar          # 随机端口
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
# → http://localhost:3000
```

### 5. 生产部署

```bash
# 前端打包
cd frontend
npm run build

# 将 dist 目录部署到 Nginx 或其他静态服务器
# Nginx 配置示例
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

## 📁 项目结构

```
spring-ai-rag/
├── pom.xml                          # 父 POM
├── frontend/                        # Vue3 前端
├── rag-gateway/                     # 网关模块
├── rag-chat/                        # 聊天/模型模块
├── rag-file/                        # 文件处理模块
├── rag-common/                      # 通用模块
└── rag-feign/                       # 远程调用模块
```
