<template>
  <div class="input-box">
    <div v-if="showPresets" class="preset-questions">
      <button v-for="q in presets" :key="q" @click="$emit('send', q)">{{ q }}</button>
    </div>
    <div class="input-wrapper">
      <textarea
        v-model="text"
        :placeholder="placeholder"
        :disabled="disabled"
        rows="1"
        @keydown.enter.exact.prevent="$emit('send', text)"
        @input="autoResize"
        ref="textareaRef"
      />
      <button
        :class="['send-btn', { active: text.trim() && !disabled }]"
        :disabled="!text.trim() || disabled"
        @click="$emit('send', text)"
      >
        <svg v-if="!disabled" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
        </svg>
        <div v-else class="send-spinner"></div>
      </button>
    </div>
    <div class="input-hint">AI 生成的内容可能不准确 · 请核实重要信息</div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  placeholder: { type: String, default: '输入你的问题...' },
  disabled: Boolean,
  presets: { type: Array, default: () => ['什么是 RAG？', 'Kafka 如何优化？', 'Redis 为什么快？', '什么是向量数据库？', 'SpringBoot 如何处理高并发？'] },
  showPresets: { type: Boolean, default: true },
})
defineEmits(['send'])

const text = ref('')
const textareaRef = ref(null)

function autoResize() {
  nextTick(() => {
    const el = textareaRef.value
    if (el) {
      el.style.height = 'auto'
      el.style.height = Math.min(el.scrollHeight, 200) + 'px'
    }
  })
}

watch(() => props.disabled, (v) => { if (!v) nextTick(() => textareaRef.value?.focus()) })
</script>

<style scoped>
.input-box {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  padding: 0 16px 20px;
}
.preset-questions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
  margin-bottom: 12px;
}
.preset-questions button {
  padding: 6px 14px;
  background: #2d2d2d;
  border: 1px solid #444;
  border-radius: 16px;
  color: #b4b4b4;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.preset-questions button:hover { background: #3d3d3d; color: #ececec; border-color: #6366f1; }

.input-wrapper {
  display: flex;
  align-items: flex-end;
  background: #2d2d2d;
  border: 1px solid #444;
  border-radius: 12px;
  padding: 8px 12px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.input-wrapper:focus-within { border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2); }

textarea {
  flex: 1;
  background: transparent;
  border: none;
  color: #ececec;
  font-size: 15px;
  line-height: 1.5;
  resize: none;
  outline: none;
  font-family: inherit;
  max-height: 200px;
}
textarea::placeholder { color: #666; }

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: #6366f1;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  margin-left: 8px;
}
.send-btn.active:hover { background: #4f46e5; transform: scale(1.05); }
.send-btn:disabled { background: #444; cursor: not-allowed; }

.send-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.input-hint { text-align: center; font-size: 12px; color: #555; margin-top: 8px; }
</style>
