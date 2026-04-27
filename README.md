# Spring AI RAG 智能简历问答系统

本项目是一个基于 Spring Cloud 微服务架构与 Spring AI 技术构建的检索增强生成（RAG）系统。系统通过模块化设计，实现了文件上传存储、基于会话的上下文管理以及大模型智能问答功能。

## 🏗️ 项目架构

项目采用前后端分离与微服务架构，主要包含以下核心模块：

| 模块名称 | 描述 |
| :--- | :--- |
| **rag-gateway** | **网关模块**。基于 Spring Cloud Gateway，集成 Nacos 注册中心，实现动态路由转发至各微服务模块。 |
| **rag-chat** | **聊天/模型模块**。系统的核心交互模块，负责调用大模型接口，处理 Prompt 并输出 AI 生成内容。 |
| **rag-file** | **文件处理模块**。负责文件的上传、下载及管理，底层使用 MinIO 作为对象存储服务。 |
| **rag-common** | **通用模块**。存放各模块共享的实体类、工具类、常量及基础配置文件。 |
| **rag-feign** | **远程调用模块**。封装了基于 OpenFeign 的客户端接口，用于模块间的服务调用。 |

---

## 💾 数据库设计与关联

系统核心业务逻辑基于 **会话 ID (`session_id`)** 进行串联，确保文件、聊天记录与会话上下文的强一致性。

### 核心表结构

1.  **`t_chat` (会话表)**
    -   记录会话的基本信息，生成唯一的 `session_id`。
    -   **关键字段**: `session_id` (会话标识), `date` (创建时间)。

2.  **`t_chat_message` (聊天记录表)**
    -   存储具体的对话内容。
    -   **关键字段**: `session_id` (关联会话), `message` (消息内容), `type` (消息类型：用户/模型), `sort` (排序)。

3.  **`t_file_info` (文件信息表)**
    -   存储上传文件的元数据信息。
    -   **关键字段**: `session_id` (关联会话), `file_name` (文件名), `create_time` (创建时间)。

### 业务关联流程

1.  **会话关联**：所有业务数据（聊天记录、上传文件）均通过 `session_id` 字段与 `t_chat` 表建立联系。
2.  **RAG 流程**：
    -   系统通过 `session_id` 在 `t_file_info` 中查询该会话关联的文件名称。
    -   **rag-chat** 模块通过 **OpenFeign** 调用 **rag-file** 模块，根据文件名获取文件流或内容。
    -   将文件内容与用户提问组合，发送给大模型进行处理。

---

## 🛠️ 数据库 DDL (SQL)

以下是系统所需的数据库建表语句：

```sql
-- 聊天记录表
CREATE TABLE `t_chat_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message` varchar(255) DEFAULT NULL COMMENT '消息内容',
  `type` varchar(50) DEFAULT NULL COMMENT '类型',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `session_id` varchar(64) DEFAULT NULL COMMENT '会话ID',
  `date` datetime DEFAULT NULL COMMENT '日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';

-- 会话表
CREATE TABLE `t_chat` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(64) DEFAULT NULL COMMENT '会话ID',
  `date` datetime DEFAULT NULL COMMENT '日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 文件信息表
CREATE TABLE `t_file_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名',
  `session_id` varchar(64) DEFAULT NULL COMMENT '会话ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';