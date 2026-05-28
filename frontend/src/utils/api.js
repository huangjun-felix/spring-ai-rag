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
    if (lang && hljs.getLanguage(lang)) return hljs.highlight(code, { language: lang }).value
    return hljs.highlightAuto(code).value
  },
})

export function renderMarkdown(text) {
  if (!text) return ''
  return marked.parse(text)
}

/**
 * 模拟流式输出
 * @param {string} text - 完整文本
 * @param {function} onChar - 每个字符回调
 * @param {function} onDone - 完成回调
 */
export function streamText(text, onChar, onDone) {
  let i = 0
  const interval = setInterval(() => {
    if (i < text.length) {
      onChar(text[i])
      i++
    } else {
      clearInterval(interval)
      onDone()
    }
  }, 15 + Math.random() * 25)
  return () => clearInterval(interval)
}

/**
 * 调用真实后端 API
 * @param {string} question
 * @param {string} sessionId
 * @param {function} onChar - 流式字符回调
 * @param {function} onDone - 完成回调
 */
export async function callBackend(question, sessionId, onChar, onDone) {
  try {
    const response = await fetch('/admin/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json;charset=utf-8' },
      body: JSON.stringify({ sessionId, message: question }),
    })

    if (!response.ok) {
      const errText = await response.text()
      throw new Error(errText || `HTTP ${response.status}`)
    }

    const fullText = await response.text()

    // 流式输出效果
    streamText(fullText, onChar, onDone)
  } catch (e) {
    onChar('⚠️ 请求失败: ' + e.message)
    onDone()
  }
}
