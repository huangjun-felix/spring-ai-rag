<template>
  <div class="app">
    <Sidebar
      :sessions="sessions"
      :current-id="currentId"
      @new="newSession"
      @select="selectSession"
      @delete="deleteSession"
    />
    <ChatWindow :messages="currentMessages" @add-message="addMessage" @update-message="updateMessage" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Sidebar from './components/Sidebar.vue'
import ChatWindow from './components/ChatWindow.vue'

const currentId = ref('sess-' + Date.now().toString(36))
const sessions = ref([])
const sessionMsgs = ref({})
const firstMsgs = ref({})

const currentMessages = computed(() => sessionMsgs.value[currentId.value] || [])

onMounted(() => {
  const saved = localStorage.getItem('rag-sessions')
  if (saved) {
    const data = JSON.parse(saved)
    sessions.value = data.sessions || []
    sessionMsgs.value = data.msgs || {}
    firstMsgs.value = data.first || {}
    if (sessions.value.length > 0) currentId.value = sessions.value[sessions.value.length - 1].id
  }
})

function saveState() {
  localStorage.setItem('rag-sessions', JSON.stringify({
    sessions: sessions.value,
    msgs: sessionMsgs.value,
    first: firstMsgs.value,
  }))
}

function genId() { return 'sess-' + Date.now().toString(36) }

function newSession() {
  const id = genId()
  currentId.value = id
  sessionMsgs.value[id] = []
  firstMsgs.value[id] = ''
}

function selectSession(id) {
  currentId.value = id
  if (!sessionMsgs.value[id]) sessionMsgs.value[id] = []
}

function deleteSession(id) {
  delete sessionMsgs.value[id]
  delete firstMsgs.value[id]
  sessions.value = sessions.value.filter(s => s.id !== id)
  if (id === currentId.value) newSession()
  saveState()
}

function addMessage(msg) {
  if (!sessionMsgs.value[currentId.value]) sessionMsgs.value[currentId.value] = []
  const msgs = sessionMsgs.value[currentId.value]
  msgs.push(msg)

  // 首次消息时记录会话
  if (msgs.length === 1 && msg.role === 'user') {
    firstMsgs.value[currentId.value] = msg.content
    if (!sessions.value.find(s => s.id === currentId.value)) {
      sessions.value.push({ id: currentId.value, title: msg.content.slice(0, 20), date: msg.time })
    }
    saveState()
  }

  if (msg.role === 'assistant' && !msg.streaming) {
    saveState()
  }
}

function updateMessage(idx, msg) {
  if (sessionMsgs.value[currentId.value]) {
    sessionMsgs.value[currentId.value][idx] = msg
  }
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #0a0a0a;
  color: #ececec;
  height: 100vh;
  overflow: hidden;
}
.app { display: flex; height: 100vh; }

/* 滚动条 */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #333; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #555; }
</style>
