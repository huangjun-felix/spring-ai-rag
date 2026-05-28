// 知识库模拟数据
export const knowledgeBase = [
  { title: 'Kafka 高性能设计.md', tags: ['kafka', '消息队列', '优化'] },
  { title: 'Redis 原理分析.pdf', tags: ['redis', '缓存', '高性能'] },
  { title: 'RAG 架构详解.md', tags: ['rag', '向量数据库', '检索增强'] },
  { title: 'SpringBoot 高并发处理.pdf', tags: ['springboot', '高并发', 'java'] },
  { title: '向量数据库原理.md', tags: ['向量', 'embedding', '相似度'] },
]

// 回答模拟数据
export const mockAnswers = {
  default:
    '这是一个很好的问题。基于知识库检索，我为您整理了以下信息：\n\n## 核心概念\n\n该系统采用了业界主流的架构设计，结合了多种高性能组件。\n\n### 技术要点\n\n- **高性能设计**：采用异步处理和批处理机制\n- **可扩展架构**：支持水平扩展和微服务化部署\n- **智能检索**：结合向量数据库实现语义级检索\n\n```java\n// 示例代码\n@Service\npublic class KnowledgeService {\n    @Autowired\n    private VectorStore vectorStore;\n    \n    public String search(String query) {\n        List<Document> docs = vectorStore.similaritySearch(query);\n        return processDocuments(docs);\n    }\n}\n```\n\n### 性能指标\n\n| 指标 | 数值 |\n|------|------|\n| 响应时间 | < 200ms |\n| 并发支持 | 10000+ QPS |\n| 数据吞吐量 | 50MB/s |\n\n> 以上信息来源于知识库中的技术文档。\n\n如需了解更多技术细节，请查阅相关文档或提出更具体的问题。',

  '什么是RAG':
    '## 什么是 RAG？\n\n**RAG**（Retrieval-Augmented Generation）即检索增强生成，是一种结合了信息检索和大型语言模型生成的架构模式。\n\n### 工作原理\n\n1. **用户提问** → 系统将问题转换为向量\n2. **向量检索** → 在知识库中查找最相关的文档片段\n3. **上下文组装** → 将检索到的内容组合成 Prompt\n4. **LLM 生成** → 大模型基于上下文生成回答\n\n### 架构优势\n\n- ✅ 减少幻觉：基于真实知识回答\n- ✅ 可追溯：每个回答都有出处\n- ✅ 易维护：更新知识库即可更新知识\n- ✅ 成本低：不需要频繁微调模型\n\n```sql\n-- 典型的知识库表结构\nCREATE TABLE t_document (\n    id BIGINT PRIMARY KEY,\n    content TEXT,\n    embedding VECTOR(1024),\n    session_id VARCHAR(64),\n    created_at TIMESTAMP\n);\n```\n\n### 应用场景\n\n| 场景 | 说明 |\n|------|------|\n| 企业知识库 | 员工自助问答 |\n| 客服系统 | 智能客服机器人 |\n| 文档检索 | 技术文档智能检索 |\n\n当前系统采用的就是 RAG 架构，结合了 ES 关键词检索和向量语义检索的混合搜索策略。',

  'Kafka如何优化':
    '## Kafka 性能优化方案\n\nKafka 作为分布式消息队列，其优化可以从以下几个维度入手：\n\n### 1. Producer 端优化\n\n```java\nProperties props = new Properties();\nprops.put("batch.size", 16384);      // 批次大小 16KB\nprops.put("linger.ms", 5);            // 等待时间 5ms\nprops.put("compression.type", "lz4"); // LZ4 压缩\nprops.put("acks", "1");               // 确认机制\n```\n\n### 2. Consumer 端优化\n\n- **批量消费**：设置 `max.poll.records` 提高单次拉取量\n- **异步处理**：消费后立即提交偏移量，异步处理业务逻辑\n- **多线程消费**：按 partition 分配独立消费线程\n\n### 3. Broker 端优化\n\n| 参数 | 推荐值 | 说明 |\n|------|--------|------|\n| num.partitions | 6-12 | 分区数越多并行度越高 |\n| log.segment.bytes | 1GB | 日志段文件大小 |\n| min.insync.replicas | 2 | 最小同步副本数 |\n\n### 4. 网络优化\n\n- **批量发送**：producer 端累积消息后批量发送\n- **零拷贝**：Kafka 底层使用 `sendfile` 实现零拷贝传输\n- **PageCache 利用**：充分利用操作系统页缓存\n\n### 性能对比\n\n优化前 vs 优化后：\n\n```\n优化前: 10,000 msg/s  →  延迟 50ms\n优化后: 100,000+ msg/s →  延迟 < 5ms\n```\n\n建议根据实际业务场景选择合适的优化策略。',

  'Redis为什么快':
    '## Redis 为什么这么快？\n\nRedis 的性能优势来源于以下几个核心设计：\n\n### 1. 纯内存操作\n\nRedis 所有数据存储在内存中，读写操作不需要访问磁盘：\n\n```\n内存访问延迟: ~100ns\n磁盘访问延迟: ~10ms\n\n相差约 100,000 倍\n```\n\n### 2. IO 多路复用\n\nRedis 使用 **epoll** 实现单线程高并发：\n\n```c\n// 伪代码：Redis 事件循环\nwhile (1) {\n    // 监听所有已注册的文件描述符\n    int n = epoll_wait(epfd, events, maxevents, timeout);\n    \n    for (int i = 0; i < n; i++) {\n        // 处理就绪的事件\n        handleEvent(events[i]);\n    }\n}\n```\n\n### 3. 高效数据结构\n\n| 结构 | 时间复杂度 | 场景 |\n|------|-----------|------|\n| SDS (简单动态字符串) | O(1) | 字符串存储 |\n| 跳表 (Skip List) | O(logN) | 有序集合 |\n| 压缩列表 (ZipList) | O(N) | 小列表/哈希 |\n| 字典 (Dict) | O(1) | Hash 表 |\n\n### 4. 单线程模型\n\n避免多线程切换开销和锁竞争：\n\n```\n多线程方案: 请求 → 锁竞争 → 线程切换 → 响应\n单线程方案: 请求 → 直接处理 → 响应\n```\n\n### 5. Pipeline 管道\n\n批量命令一次网络往返：\n\n```redis\nMULTI\nSET key1 value1\nSET key2 value2\nGET key1\nEXEC\n```\n\n### 性能基准\n\n```bash\n$ redis-benchmark -t set,get -n 100000 -q\nSET: 125000 requests/s\nGET: 142857 requests/s\n```\n\n当前系统使用 Redis 作为向量存储和会话缓存，充分利用了其高性能特性。',

  'SpringBoot如何处理高并发':
    '## SpringBoot 高并发处理方案\n\n在微服务架构中，SpringBoot 处理高并发需要从多个层面优化：\n\n### 1. 线程池配置\n\n```java\n@Configuration\npublic class ThreadPoolConfig {\n    @Bean(\"taskAsync\")\n    public Executor taskExecutor() {\n        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();\n        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());\n        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);\n        executor.setQueueCapacity(500);\n        executor.setThreadNamePrefix(\"async-\");\n        executor.initialize();\n        return executor;\n    }\n}\n```\n\n### 2. 数据库连接池\n\n```yaml\nspring:\n  datasource:\n    hikari:\n      maximum-pool-size: 20\n      minimum-idle: 5\n      connection-timeout: 30000\n      idle-timeout: 600000\n```\n\n### 3. 缓存策略\n\n| 层级 | 技术 | 用途 |\n|------|------|------|\n| L1 | Caffeine | 本地缓存热点数据 |\n| L2 | Redis | 分布式缓存 |\n| L3 | MySQL | 持久化存储 |\n\n### 4. 异步处理\n\n```java\n@Async("taskAsync")\npublic void asyncProcess(String data) {\n    // 耗时操作，不阻塞主线程\n    heavyComputation(data);\n}\n```\n\n### 5. 限流与降级\n\n```java\n@SentinelResource(value = "query", blockHandler = "handleBlock")\npublic Result query(String id) {\n    return service.query(id);\n}\n\npublic Result handleBlock(String id, BlockException e) {\n    return Result.error("系统繁忙，请稍后重试");\n}\n```\n\n### 性能指标参考\n\n| 优化项 | 优化前 | 优化后 |\n|--------|--------|--------|\n| QPS | 500 | 10000+ |\n| 响应时间 | 500ms | 50ms |\n| CPU 利用率 | 80% | 40% |\n\n当前 RAG 系统采用了线程池 + Redis 缓存 + 异步消息保存的组合策略。',

  '什么是向量数据库':
    '## 向量数据库详解\n\n向量数据库（Vector Database）是专门用于存储和检索**高维向量**的数据库系统。\n\n### 核心概念\n\n**向量（Embedding）**：将文本、图片等转换为浮点数数组，例如：\n\n```json\n{\n  "text": "SpringBoot 高并发",\n  "embedding": [0.12, -0.45, 0.78, ..., 0.33],\n  "dimensions": 1024\n}\n```\n\n### 相似度计算\n\n常用算法：\n\n| 算法 | 公式 | 适用场景 |\n|------|------|----------|\n| 余弦相似度 | cos(θ) = A·B / \|A\|·\|B\| | 文本语义匹配 |\n| 欧氏距离 | d = √Σ(aᵢ-bᵢ)² | 图像检索 |\n| 曼哈顿距离 | d = Σ\|aᵢ-bᵢ\| | 低维数据 |\n\n### 主流向量数据库\n\n```sql\n-- Redis Vector Store (当前系统使用)\nFT.CREATE idx:spring-ai-index ON HASH PREFIX 1 doc: SCHEMA \n  vector VECTOR FLAT 6 TYPE FLOAT32 DIM 1024 DISTANCE_METRIC COSINE\n  session_id TAG\n  section_title TEXT\n```\n\n### RAG 系统中的向量检索流程\n\n```\n用户问题 → text-embedding-v3 → [1024维向量]\n                                ↓\n                    向量数据库 COSINE 相似度搜索\n                                ↓\n         Top K 结果 → 按相似度排序 → 返回上下文\n```\n\n### 关键参数\n\n| 参数 | 默认值 | 说明 |\n|------|--------|------|\n| topK | 10 | 返回最相似的文档数 |\n| similarityThreshold | 0.5 | 最低相似度阈值 |\n| dimensions | 1024 | 向量维度 |\n\n当前系统使用 Redis 作为向量存储，结合 text-embedding-v3 模型实现 1024 维向量的存储和检索。',
}

// 预设问题
export const presetQuestions = [
  '什么是RAG',
  'Kafka如何优化',
  'Redis为什么快',
  'SpringBoot如何处理高并发',
  '什么是向量数据库',
]
