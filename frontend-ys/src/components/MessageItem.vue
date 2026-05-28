<template>
  <div :class="['message-wrapper', message.role]">
    <div v-if="message.role === 'assistant'" class="avatar ai-avatar">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
      </svg>
    </div>
    <div class="message-content">
      <div class="message-sender">{{ message.role === 'user' ? '你' : 'AI 助手' }}</div>
      <div class="message-body">
        <!-- 思考动画 -->
        <div v-if="message.thinking" class="thinking-steps">
          <div v-for="(step, i) in thinkingSteps" :key="i"
               :class="['thinking-step', { active: currentStep >= i, done: currentStep > i }]">
            <svg v-if="currentStep > i" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            <div v-else-if="currentStep === i" class="thinking-spinner"></div>
            <span>{{ step }}</span>
          </div>
        </div>

        <!-- 知识命中 -->
        <div v-if="message.hitInfo" class="hit-info">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>已检索知识库，命中相关文件</span>
        </div>

        <!-- Markdown 内容 -->
        <div v-if="message.displayContent" class="markdown-body" v-html="renderedHtml"></div>

        <!-- 打字机光标 -->
        <span v-if="message.role === 'assistant' && message.streaming" class="cursor"></span>
      </div>
      <div v-if="message.time" class="message-time">{{ formatTime(message.time) }}</div>
    </div>
    <div v-if="message.role === 'user'" class="avatar user-avatar">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
      </svg>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import sql from 'highlight.js/lib/languages/sql'
import json from 'highlight.js/lib/languages/json'
import bash from 'highlight.js/lib/languages/bash'
import javascript from 'highlight.js/lib/languages/javascript'
import yaml from 'highlight.js/lib/languages/yaml'

hljs.registerLanguage('java', java)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('json', json)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('redis', javascript)

marked.setOptions({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
})

const props = defineProps({
  message: { type: Object, required: true },
})

const thinkingSteps = ['正在检索知识库...', '正在分析上下文...', '正在生成回答...']
const currentStep = ref(-1)

watch(() => props.message.thinking, (val) => {
  if (val) {
    currentStep.value = 0
    let i = 0
    const timer = setInterval(() => {
      i++
      if (i <= thinkingSteps.length) currentStep.value = i
      if (i >= thinkingSteps.length) clearInterval(timer)
    }, 800)
  }
})

const renderedHtml = computed(() => {
  if (!props.message.displayContent) return ''
  return marked.parse(props.message.displayContent)
})

function formatTime(ts) {
  if (!ts) return ''
  return new Date(ts).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.message-wrapper {
  display: flex;
  gap: 16px;
  padding: 24px 0;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  animation: fadeInUp 0.3s ease-out;
}
.message-wrapper.user { flex-direction: row-reverse; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}
.ai-avatar { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.user-avatar { background: linear-gradient(135deg, #3b82f6, #2563eb); }

.message-content { flex: 1; min-width: 0; }
.message-sender { font-size: 13px; font-weight: 600; color: #ececec; margin-bottom: 8px; }
.message-body {
  line-height: 1.7;
  font-size: 15px;
  color: #d4d4d4;
}
.message-wrapper.user .message-sender { text-align: right; }
.message-wrapper.user .message-body {
  background: #2d2d2d;
  padding: 12px 16px;
  border-radius: 12px;
  color: #e5e5e5;
}
.message-time { font-size: 11px; color: #555; margin-top: 8px; }

/* 思考动画 */
.thinking-steps { margin-bottom: 12px; }
.thinking-step {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #555;
  padding: 4px 0;
  transition: all 0.3s;
}
.thinking-step.active { color: #888; }
.thinking-step.done { color: #22c55e; }
.thinking-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #333;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 知识命中 */
.hit-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #3b82f6;
  padding: 6px 10px;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 6px;
  margin-bottom: 12px;
}

/* 打字机光标 */
.cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  background: #6366f1;
  animation: blink 0.6s infinite;
  vertical-align: text-bottom;
  margin-left: 2px;
}
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

/* Markdown 样式 */
:deep(.markdown-body) { color: #d4d4d4; }
:deep(.markdown-body h1), :deep(.markdown-body h2), :deep(.markdown-body h3) {
  color: #ececec;
  margin: 16px 0 8px;
}
:deep(.markdown-body h2) { font-size: 1.2em; border-bottom: 1px solid #333; padding-bottom: 4px; }
:deep(.markdown-body h3) { font-size: 1.05em; }
:deep(.markdown-body p) { margin: 8px 0; }
:deep(.markdown-body code) {
  background: #1e1e1e;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  color: #e879f9;
}
:deep(.markdown-body pre) {
  background: #1a1a2e !important;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
  border: 1px solid #333;
}
:deep(.markdown-body pre code) {
  background: transparent;
  padding: 0;
  color: #d4d4d4;
}
:deep(.markdown-body table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
}
:deep(.markdown-body th), :deep(.markdown-body td) {
  border: 1px solid #333;
  padding: 8px 12px;
  font-size: 13px;
}
:deep(.markdown-body th) { background: #2d2d2d; color: #ececec; }
:deep(.markdown-body blockquote) {
  border-left: 3px solid #6366f1;
  padding-left: 16px;
  color: #888;
  margin: 12px 0;
}
:deep(.markdown-body ul), :deep(.markdown-body ol) { padding-left: 24px; }
:deep(.markdown-body li) { margin: 4px 0; }
:deep(.markdown-body strong) { color: #60a5fa; font-weight: 600; }
</style>
