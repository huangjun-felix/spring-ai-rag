<template>
  <div class="flex h-screen bg-gray-100">
    <!-- 左侧会话列表 -->
    <div class="w-64 bg-white border-r border-gray-200 flex flex-col shrink-0">
      <div class="p-3 border-b border-gray-100">
        <el-button type="primary" class="w-full" @click="newSession">
          <el-icon><Plus /></el-icon> 新建对话
        </el-button>
      </div>
      <div class="flex-1 overflow-y-auto">
        <div v-if="sessions.length === 0" class="p-6 text-center text-gray-400 text-sm">暂无对话</div>
        <div v-for="s in sessions" :key="s.sessionId"
             :class="['px-3 py-2.5 cursor-pointer border-b border-gray-50 transition-colors',
                       s.sessionId === currentId ? 'bg-blue-50 border-l-2 border-l-blue-500' : 'hover:bg-gray-50']"
             @click="selectSession(s.sessionId)">
          <div class="text-sm font-medium text-gray-700 truncate">{{ getSessionTitle(s.sessionId) }}</div>
          <div class="text-xs text-gray-400">{{ fmtTime(s.date) }}</div>
        </div>
      </div>
    </div>

    <!-- 右侧主区域 -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- 上传栏 -->
      <div class="h-12 px-4 flex items-center border-b border-gray-200 bg-white gap-2">
        <span class="text-sm text-gray-500">会话 {{ currentId.slice(-8) }}</span>
        <div class="flex-1" />
        <el-button type="primary" size="small" plain @click="showUploadDialog = true">
          <el-icon><Upload /></el-icon> 上传 PDF
        </el-button>
      </div>

      <!-- 文件上传进度弹窗 -->
      <el-dialog v-model="showUploadDialog" title="上传文件" width="420px" :close-on-click-modal="false"
                 @opened="resetUploadState">
        <div class="py-4">
          <input ref="fileInput" type="file" accept=".pdf" class="hidden" @change="onFileSelected" />
          <div v-if="!uploadProgress.active" class="text-center text-gray-400 py-6 cursor-pointer border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-400" @click="$refs.fileInput?.click()">
            点击选择 PDF 文件
          </div>
          <div v-else>
            <div class="flex items-center gap-2 mb-2">
              <el-icon v-if="uploadProgress.status === 'exception'" color="#f56c6c"><CircleCloseFilled /></el-icon>
              <el-icon v-else-if="uploadProgress.status === 'success'" color="#67c23a"><CircleCheckFilled /></el-icon>
              <el-icon v-else class="is-loading text-blue-500"><Loading /></el-icon>
              <span class="text-sm text-gray-600">{{ uploadProgress.name }}</span>
            </div>
            <el-progress :percentage="uploadProgress.percent" :stroke-width="8" :status="uploadProgress.status" />
            <div class="text-xs text-gray-400 mt-1">{{ uploadProgress.tip }}</div>
          </div>
        </div>
      </el-dialog>

      <!-- 文件已上传提示 -->
      <div v-if="fileUploaded[currentId]" class="px-4 py-1.5 bg-green-50 border-b border-green-200 flex items-center gap-2">
        <el-icon color="#67c23a"><CircleCheckFilled /></el-icon>
        <span class="text-sm text-green-700">文件已解析完成，现在可以提问</span>
      </div>

      <!-- 消息区 -->
      <div ref="msgBox" class="flex-1 overflow-y-auto px-5 py-4 space-y-3">
        <div v-if="currentMsgs.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400">
          <el-icon size="56" color="#dcdfe6"><ChatDotSquare /></el-icon>
          <p class="mt-3 text-base">知识库智能问答</p>
          <p class="mt-1 text-sm">上传文件后开始对话</p>
        </div>

        <div v-for="(m, i) in currentMsgs" :key="i" :class="['flex gap-2', m.role === 'user' ? 'justify-end' : '']">
          <div v-if="m.role === 'assistant'" class="w-7 h-7 rounded-full bg-blue-500 flex items-center justify-center shrink-0 mt-1">
            <el-icon size="14" color="white"><ChatDotSquare /></el-icon>
          </div>
          <div :class="['max-w-[75%] rounded-xl px-3 py-2 text-sm leading-relaxed',
            m.role === 'user' ? 'bg-blue-500 text-white rounded-br-md'
              : m.error ? 'bg-red-50 border border-red-200 text-red-700'
              : 'bg-gray-50 border border-gray-100 text-gray-800 rounded-bl-md']">
            <div v-if="m.role === 'user'" class="whitespace-pre-wrap">{{ m.content }}</div>
            <div v-else class="markdown-body" v-html="renderMd(m.content)" />
            <div class="text-xs mt-1 opacity-50">{{ fmtTime(m.time) }}</div>
          </div>
          <div v-if="m.role === 'user'" class="w-7 h-7 rounded-full bg-gray-300 flex items-center justify-center shrink-0 mt-1">
            <el-icon size="14" color="white"><User /></el-icon>
          </div>
        </div>
        <div v-if="loading && currentMsgs.length > 0 && currentMsgs[currentMsgs.length-1].role === 'user'" class="flex items-center gap-2 text-sm text-gray-400 px-1">
          <el-icon class="is-loading"><Loading /></el-icon><span>AI 思考中...</span>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="border-t border-gray-200 bg-white px-4 py-2.5 shrink-0">
        <div class="flex gap-2">
          <el-input v-model="input" type="textarea" :rows="1" :autosize="{ minRows:1, maxRows:4 }"
                    placeholder="输入问题..." resize="none" @keydown.enter.exact.prevent="send" />
          <el-button type="primary" :disabled="!input.trim() || loading" :loading="loading" @click="send">
            <el-icon><Promotion /></el-icon> 发送
          </el-button>
        </div>
        <div class="flex gap-1.5 mt-1.5 flex-wrap">
          <el-tag v-for="h in hints" :key="h" size="small" class="cursor-pointer hover:bg-blue-100"
                  @click="input = h; send()">{{ h }}</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const input = ref('')
const msgBox = ref(null)
const currentId = ref(genId())
const sessions = ref([])
const sessionMsgs = ref({})
const fileUploaded = ref({})       // 按会话隔离: { sessionId: true }
const showUploadDialog = ref(false)
const fileInput = ref(null)
const uploadProgress = ref({ active: false, name: '', percent: 0, status: '', tip: '' })
const firstMsgs = ref({})
const hints = ['文件讲了什么？', '总结要点', '有哪些技术栈？']

const currentMsgs = computed(() => {
  const id = currentId.value
  return sessionMsgs.value[id] ? [...sessionMsgs.value[id]] : []
})

// 页面加载
onMounted(async () => {
  await loadSessions()
  if (sessions.value.length > 0) {
    selectSession(sessions.value[sessions.value.length - 1].sessionId)
  }
})

function genId() { return 'sess-' + Date.now().toString(36) }

async function loadSessions() {
  try {
    const r = await fetch('/admin/common/sessions')
    console.log('[loadSessions] status:', r.status, r.statusText)
    if (r.ok) {
      const data = await r.json()
      console.log('[loadSessions] sessions:', data)
      sessions.value = data
    } else {
      const text = await r.text()
      console.warn('[loadSessions] error:', r.status, text)
    }
  } catch (e) {
    console.warn('[loadSessions] exception:', e)
  }
}

// 选择会话
function selectSession(id) {
  currentId.value = id
  if (!sessionMsgs.value[id]) sessionMsgs.value[id] = []
  scrollDown()
}

function newSession() {
  const id = genId()
  currentId.value = id
  sessionMsgs.value[id] = []
  firstMsgs.value[id] = ''
}

function getSessionTitle(sid) {
  return firstMsgs.value[sid] ? firstMsgs.value[sid].slice(0, 16) : sid.slice(-8)
}

async function send() {
  const t = input.value.trim()
  if (!t || loading.value) return

  // 确保当前会话有消息数组
  if (!sessionMsgs.value[currentId.value]) sessionMsgs.value[currentId.value] = []

  // 记录第一条消息作为标题
  if (sessionMsgs.value[currentId.value].length === 0) {
    firstMsgs.value[currentId.value] = t
  }

  sessionMsgs.value[currentId.value].push({ role: 'user', content: t, time: Date.now() })
  input.value = ''
  loading.value = true
  scrollDown()

  try {
    const r = await fetch('/admin/ai/chat', {
      method: 'POST', headers: { 'Content-Type': 'application/json;charset=utf-8' },
      body: JSON.stringify({ sessionId: currentId.value, message: t }),
    })
    if (!r.ok) throw new Error(r.status)
    const txt = await r.text()
    sessionMsgs.value[currentId.value].push({ role: 'assistant', content: txt, time: Date.now() })
    // 刷新会话列表（后端已创建新会话）
    await loadSessions()
  } catch (e) {
    sessionMsgs.value[currentId.value].push({ role: 'assistant', content: '请求失败: ' + e.message, time: Date.now(), error: true })
  } finally { loading.value = false; scrollDown() }
}

// 文件上传
function resetUploadState() {
  uploadProgress.value = { active: false, name: '', percent: 0, status: '', tip: '' }
  if (fileInput.value) fileInput.value.value = ''
}

function onFileSelected(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!file.name.endsWith('.pdf')) { ElMessage.error('仅支持 PDF'); resetUploadState(); return }
  if (file.size > 100*1024*1024) { ElMessage.error('最大 100MB'); resetUploadState(); return }

  uploadProgress.value = { active: true, name: file.name, percent: 0, status: '', tip: '正在上传...' }

  const formData = new FormData()
  formData.append('file', file)

  const xhr = new XMLHttpRequest()
  xhr.open('POST', '/admin/file/upload/' + currentId.value, true)

  xhr.upload.onprogress = (e) => {
    if (e.lengthComputable) {
      const pct = Math.round(e.loaded / e.total * 100)
      uploadProgress.value.percent = pct
      uploadProgress.value.tip = `正在上传... ${pct}%`
    }
  }

  xhr.onload = () => {
    if (xhr.status >= 200 && xhr.status < 300) {
      uploadProgress.value.percent = 100
      uploadProgress.value.status = 'success'
      uploadProgress.value.tip = '上传完成，AI 正在解析 PDF...'
      setTimeout(() => {
        showUploadDialog.value = false
        fileUploaded.value[currentId.value] = true
        ElMessage.success('文件上传并解析成功')
        loadSessions()
      }, 800)
    } else {
      let errMsg = xhr.responseText || xhr.statusText || '未知错误'
      uploadProgress.value.status = 'exception'
      uploadProgress.value.tip = '上传失败: ' + errMsg
      ElMessage.error('上传失败: ' + errMsg)
    }
  }

  xhr.onerror = () => {
    uploadProgress.value.status = 'exception'
    uploadProgress.value.tip = '网络错误，请检查后端是否运行'
    ElMessage.error('网络错误，上传失败')
  }

  xhr.send(formData)
}

function scrollDown() { nextTick(() => { if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight }) }
watch(() => currentMsgs.value.length, scrollDown)
function renderMd(text) {
  if (!text) return ''
  return text.replace(/^### (.+)$/gm, '<h3>$1</h3>').replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>').replace(/\n\n/g, '</p><p>').replace(/\n/g, '<br>')
}
function fmtTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return d.toLocaleTimeString('zh-CN', {hour:'2-digit',minute:'2-digit'})
}
</script>
