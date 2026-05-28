<template>
  <div class="sidebar">
    <button class="new-chat-btn" @click="$emit('new')">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
      </svg>
      新建对话
    </button>
    <div class="session-list">
      <div v-if="sessions.length === 0" class="empty-hint">暂无对话</div>
      <div v-for="s in sessions" :key="s.id"
           :class="['session-item', s.id === currentId ? 'active' : '']"
           @click="$emit('select', s.id)">
        <svg class="session-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="session-title">{{ s.title || '新对话' }}</span>
        <button class="delete-btn" @click.stop="$emit('delete', s.id)" title="删除">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ sessions: { type: Array, default: () => [] }, currentId: String })
defineEmits(['new', 'select', 'delete'])
</script>

<style scoped>
.sidebar {
  width: 260px;
  background: #171717;
  border-right: 1px solid #2d2d2d;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.new-chat-btn {
  margin: 12px;
  padding: 10px 16px;
  background: transparent;
  border: 1px solid #4a4a4a;
  border-radius: 8px;
  color: #ececec;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}
.new-chat-btn:hover { background: #2d2d2d; border-color: #666; }
.session-list { flex: 1; overflow-y: auto; padding: 0 8px; }
.empty-hint { text-align: center; color: #666; font-size: 13px; padding: 40px 0; }
.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  color: #b4b4b4;
  font-size: 13px;
}
.session-item:hover { background: #2d2d2d; color: #ececec; }
.session-item.active { background: #2d2d2d; color: #ececec; }
.session-icon { flex-shrink: 0; opacity: 0.6; }
.session-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.delete-btn {
  opacity: 0;
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  padding: 2px;
  transition: all 0.15s;
}
.session-item:hover .delete-btn { opacity: 1; }
.delete-btn:hover { color: #f56c6c; }
</style>
