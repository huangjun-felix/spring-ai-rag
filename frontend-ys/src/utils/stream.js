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
  }, 20 + Math.random() * 30) // 20-50ms 随机延迟，模拟真实打字速度
  return () => clearInterval(interval)
}

/**
 * 根据用户问题匹配知识库和回答
 */
export function matchAnswer(question) {
  const q = question.toLowerCase()

  // 关键词匹配
  for (const [key, answer] of Object.entries(window.__MOCK_ANSWERS || {})) {
    if (key === 'default') continue
    if (q.includes(key.toLowerCase()) || key.toLowerCase().includes(q)) {
      return answer
    }
  }

  // 标签匹配
  const kb = window.__MOCK_KB || []
  const answers = window.__MOCK_ANSWERS || {}
  const matchedKB = []
  for (const item of kb) {
    const hit = item.tags.some(t => q.includes(t.toLowerCase()))
    if (hit) matchedKB.push(item.title)
  }

  if (matchedKB.length > 0) {
    return answers.default.replace(
      '当前系统采用了业界主流的架构设计',
      `当前系统采用了业界主流的架构设计\n\n**已命中知识片段：**\n${matchedKB.map(t => '- ' + t).join('\n')}`
    )
  }

  return answers.default
}
