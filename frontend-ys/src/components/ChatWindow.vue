<template>
  <div class="chat-window" ref="chatRef">
    <div v-if="messages.length === 0" class="welcome">
      <div class="welcome-logo">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="url(#grad)" stroke-width="1.5">
          <defs><linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#6366f1"/><stop offset="100%" stop-color="#8b5cf6"/></linearGradient></defs>
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
        </svg>
      </div>
      <h1 class="welcome-title">AI RAG 知识库</h1>
      <p class="welcome-sub">检索增强生成 · 智能问答 · 混合检索</p>
    </div>

    <MessageItem v-for="(m, i) in messages" :key="i" :message="m" />

    <InputBox
      :disabled="isStreaming"
      :show-presets="messages.length === 0"
      @send="handleSend"
    />
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import MessageItem from './MessageItem.vue'
import InputBox from './InputBox.vue'
import { streamText, matchAnswer } from '../utils/stream.js'

const props = defineProps({ messages: { type: Array, default: () => [] } })
const emit = defineEmits(['add-message', 'update-message'])
const isStreaming = ref(false)
const chatRef = ref(null)

function scrollToBottom() {
  nextTick(() => {
    const el = chatRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

async function handleSend(text) {
  if (!text.trim() || isStreaming.value) return
  const question = text.trim()

  // 用户消息
  emit('add-message', { role: 'user', content: question, time: Date.now() })

  // AI 消息（思考中）
  const aiMsgIdx = props.messages.length
  emit('add-message', {
    role: 'assistant', content: '', displayContent: '',
    thinking: true, streaming: false, hitInfo: false, time: Date.now(),
  })

  // 模拟思考延迟
  await new Promise(r => setTimeout(r, 2500))

  // 停止思考，开始流式输出
  const answer = matchAnswer(question)
  const msg = { ...props.messages[aiMsgIdx] }
  msg.thinking = false
  msg.hitInfo = true
  msg.streaming = true
  emit('update-message', aiMsgIdx, msg)

  let current = ''
  isStreaming.value = true
  scrollToBottom()

  await new Promise(resolve => {
    streamText(answer, (char) => {
      current += char
      emit('update-message', aiMsgIdx, { ...msg, displayContent: current })
      scrollToBottom()
    }, () => {
      emit('update-message', aiMsgIdx, { ...msg, displayContent: current, streaming: false })
      isStreaming.value = false
      resolve()
    })
  })
}
</script>

<style scoped>
.chat-window {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}
.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  animation: fadeInUp 0.5s ease-out;
}
.welcome-logo {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(99,102,241,0.2), rgba(139,92,246,0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}
.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: #ececec;
  margin: 0 0 8px;
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.welcome-sub { color: #888; font-size: 15px; margin: 0; }
</style>
