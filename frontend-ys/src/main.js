import { createApp } from 'vue'
import App from './App.vue'
import { mockAnswers, knowledgeBase } from './mock/mockData.js'

// 全局挂载 mock 数据
window.__MOCK_ANSWERS = mockAnswers
window.__MOCK_KB = knowledgeBase

createApp(App).mount('#app')
