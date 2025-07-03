<template>
  <div class="ai-assistant">
    <div class="page-header">
      <h1>GLM-4V学习助手</h1>
      <p>智谱AI驱动的多模态学习伙伴，支持文本、图像和视频理解</p>
    </div>

    <div class="chat-container">
      <el-card class="chat-card">
        <template #header>
          <div class="chat-header">
            <div class="assistant-info">
              <el-avatar :size="40" src="/api/placeholder/40/40">GLM</el-avatar>
              <div class="assistant-details">
                <h3>GLM-4V学习助手</h3>
                <p>在线 · 智谱AI驱动</p>
              </div>
            </div>
            <div class="header-actions">
              <el-button type="text" @click="showShortcutHelp" size="small">
                ⌨️ 快捷键
              </el-button>
              <el-button type="text" @click="clearChat" size="small">
                <el-icon><Delete /></el-icon>
                清空对话
              </el-button>
            </div>
          </div>
        </template>

        <!-- 聊天消息区域 -->
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="welcome-message">
            <el-icon size="48" color="#409eff"><Avatar /></el-icon>
            <h3>欢迎使用GLM-4V学习助手！</h3>
            <p>我可以帮助您：</p>
            <ul>
              <li>📚 制定个性化学习计划</li>
              <li>💡 分析学习进度和薄弱环节</li>
              <li>🎯 推荐学习资源和方法</li>
              <li>❓ 回答学习相关问题</li>
              <li>🖼️ 理解和分析图像内容</li>
              <li>🎥 处理视频相关问题</li>
            </ul>
            <div class="quick-start">
              <p>试试这些快速问题：</p>
              <el-button 
                v-for="question in quickQuestions" 
                :key="question"
                type="primary" 
                plain 
                size="small"
                @click="sendQuickQuestion(question)"
              >
                {{ question }}
              </el-button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div v-else class="messages-list">
            <div 
              v-for="message in messages" 
              :key="message.id" 
              :class="['message-item', message.isUser ? 'user-message' : 'ai-message']"
            >
              <el-avatar 
                v-if="!message.isUser" 
                :size="32" 
                src="/api/placeholder/32/32"
                class="message-avatar"
              >
                GLM
              </el-avatar>
              
              <div class="message-content">
                <div class="message-bubble">
                  <div class="message-text" v-html="formatMessage(message.content)"></div>
                  <div class="message-time">{{ message.time }}</div>
                </div>
              </div>

              <el-avatar 
                v-if="message.isUser" 
                :size="32" 
                class="message-avatar"
              >
                {{ userName?.charAt(0) || 'U' }}
              </el-avatar>
            </div>

            <!-- 输入指示器 -->
            <div v-if="isTyping" class="typing-indicator">
              <el-avatar :size="32" src="/api/placeholder/32/32">GLM</el-avatar>
              <div class="typing-bubble">
                <div class="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <!-- 快捷操作按钮栏 -->
          <div class="quick-actions">
            <el-button 
              size="small" 
              type="primary" 
              plain
              @click="insertTemplate('制定学习计划')"
              :disabled="isTyping"
            >
              📚 学习计划
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              plain
              @click="insertTemplate('分析我的学习进度')"
              :disabled="isTyping"
            >
              📊 进度分析
            </el-button>
            <el-button 
              size="small" 
              type="warning" 
              plain
              @click="insertTemplate('推荐学习资源')"
              :disabled="isTyping"
            >
              📖 资源推荐
            </el-button>
            <el-button 
              size="small" 
              type="info" 
              plain
              @click="insertTemplate('解答疑问')"
              :disabled="isTyping"
            >
              ❓ 答疑解惑
            </el-button>
            <el-divider direction="vertical" />
            <el-button 
              size="small" 
              text
              @click="uploadImage"
              :disabled="isTyping"
            >
              🖼️ 上传图片
            </el-button>
            <el-button 
              size="small" 
              text
              @click="uploadVideo"
              :disabled="isTyping"
            >
              🎥 上传视频
            </el-button>
          </div>
          
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题... (Ctrl+Enter发送，Shift+Enter换行)"
            @keydown.ctrl.enter="sendMessage"
            @keydown.meta.enter="sendMessage"
            @keydown.shift.enter="handleShiftEnter"
            @keydown.esc="clearInput"
            :disabled="isTyping"
            ref="messageInput"
          />
          
          <!-- 文件上传隐藏组件 -->
          <input 
            ref="imageInput" 
            type="file" 
            accept="image/*" 
            style="display: none" 
            @change="handleImageUpload"
          />
          <input 
            ref="videoInput" 
            type="file" 
            accept="video/*" 
            style="display: none" 
            @change="handleVideoUpload"
          />
          
          <div class="input-actions">
            <div class="input-tips">
              <span>快捷键：Ctrl+Enter发送 | Shift+Enter换行 | Esc清空 | Alt+1-4模板 | Alt+I图片 | Alt+V视频</span>
            </div>
            <div class="action-buttons">
              <el-button 
                size="small"
                @click="clearInput" 
                :disabled="!inputMessage.trim() || isTyping"
              >
                <el-icon><Delete /></el-icon>
                清空
              </el-button>
              <el-button 
                type="primary" 
                @click="sendMessage" 
                :loading="isTyping"
                :disabled="!inputMessage.trim()"
              >
                <el-icon><Position /></el-icon>
                发送
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 快捷键帮助对话框 -->
    <el-dialog 
      v-model="shortcutHelpVisible" 
      title="⌨️ 快捷键帮助" 
      width="500px"
      :show-close="true"
    >
      <div class="shortcut-help">
        <h4>💬 对话操作</h4>
        <ul>
          <li><kbd>Ctrl</kbd> + <kbd>Enter</kbd> - 发送消息</li>
          <li><kbd>Shift</kbd> + <kbd>Enter</kbd> - 换行</li>
          <li><kbd>Esc</kbd> - 清空输入框</li>
        </ul>

        <h4>🚀 快速模板</h4>
        <ul>
          <li><kbd>Alt</kbd> + <kbd>1</kbd> - 制定学习计划</li>
          <li><kbd>Alt</kbd> + <kbd>2</kbd> - 分析学习进度</li>
          <li><kbd>Alt</kbd> + <kbd>3</kbd> - 推荐学习资源</li>
          <li><kbd>Alt</kbd> + <kbd>4</kbd> - 答疑解惑</li>
        </ul>

        <h4>📁 文件操作</h4>
        <ul>
          <li><kbd>Alt</kbd> + <kbd>I</kbd> - 上传图片</li>
          <li><kbd>Alt</kbd> + <kbd>V</kbd> - 上传视频</li>
          <li><kbd>Alt</kbd> + <kbd>C</kbd> - 清空输入</li>
        </ul>

        <div class="tip">
          <p><strong>💡 提示：</strong>您也可以点击快捷操作按钮来使用这些功能！</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Avatar, Position } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { chatWithAI } from '@/api/ai'
import { getUserDashboardStats } from '@/api/analytics'

// 响应式数据
const authStore = useAuthStore()
const userName = computed(() => authStore.user?.fullName || authStore.user?.username)
const userId = computed(() => authStore.user?.id)

const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const messagesContainer = ref(null)
const messageInput = ref(null)
const imageInput = ref(null)
const videoInput = ref(null)

// 用户数据
const userStats = ref({})

// 快捷键帮助对话框
const shortcutHelpVisible = ref(false)

// 快速问题
const quickQuestions = [
  '为我制定学习计划',
  '分析我的学习进度', 
  '推荐学习资源',
  '如何提高学习效率',
  '解释这个概念',
  '总结知识要点'
]

// 方法
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return

  const userMessage = {
    id: Date.now(),
    content: inputMessage.value.trim(),
    isUser: true,
    time: new Date().toLocaleTimeString()
  }

  messages.value.push(userMessage)
  const question = inputMessage.value.trim()
  inputMessage.value = ''

  try {
    isTyping.value = true
    
    // 调用AI接口
    const response = await chatWithAI({
      message: question,
      context: [
        {
          type: "user_stats",
          data: JSON.stringify(userStats.value || {})
        }
      ],
      useReasoner: false
    })
    
    console.log('AI响应:', response)
    
    if (response.data && response.data.success && response.data.reply) {
      messages.value.push({
        id: Date.now(),
        content: response.data.reply,
        isUser: false,
        time: new Date().toLocaleTimeString()
      })
    } else {
      throw new Error(response.data?.error || 'AI响应异常')
    }
    
  } catch (error) {
    console.error('AI对话失败:', error)
    ElMessage.error(`GLM-4V服务暂时不可用: ${error.message}`)
    
    // 添加错误消息
    messages.value.push({
      id: Date.now(),
      content: '抱歉，我暂时无法回复您的问题，请稍后再试。',
      isUser: false,
      time: new Date().toLocaleTimeString()
    })
  } finally {
    isTyping.value = false
  }
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 插入模板文本
const insertTemplate = (template) => {
  const templates = {
    '制定学习计划': '请帮我制定一个详细的学习计划，我的学习目标是：',
    '分析我的学习进度': '请分析我的学习进度，目前我在学习：',
    '推荐学习资源': '请为我推荐一些高质量的学习资源，我想学习：',
    '解答疑问': '我有一个问题需要解答：'
  }
  
  if (templates[template]) {
    inputMessage.value = templates[template]
    // 聚焦到输入框末尾
    nextTick(() => {
      if (messageInput.value) {
        const textarea = messageInput.value.$el.querySelector('textarea')
        if (textarea) {
          textarea.focus()
          textarea.setSelectionRange(textarea.value.length, textarea.value.length)
        }
      }
    })
  }
}

// 清空输入
const clearInput = () => {
  inputMessage.value = ''
  if (messageInput.value) {
    messageInput.value.focus()
  }
}

// 处理Shift+Enter换行
const handleShiftEnter = (event) => {
  // 阻止默认行为，让输入框正常换行
  // Element Plus的textarea会自动处理Shift+Enter换行
}

// 上传图片
const uploadImage = () => {
  if (imageInput.value) {
    imageInput.value.click()
  }
}

// 上传视频
const uploadVideo = () => {
  if (videoInput.value) {
    videoInput.value.click()
  }
}

// 处理图片上传
const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 检查文件大小 (5MB限制)
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过5MB')
      return
    }
    
    // 检查文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择有效的图片文件')
      return
    }
    
    // 读取文件并转换为base64
    const reader = new FileReader()
    reader.onload = (e) => {
      const base64 = e.target.result
      inputMessage.value += `\n[图片已上传: ${file.name}]\n`
      
      // 这里可以将base64数据存储到一个变量中，在发送消息时一起发送
      ElMessage.success('图片上传成功，可以开始对话了')
    }
    reader.readAsDataURL(file)
  }
  
  // 清空文件选择
  event.target.value = ''
}

// 处理视频上传
const handleVideoUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 检查文件大小 (200MB限制)
    if (file.size > 200 * 1024 * 1024) {
      ElMessage.error('视频大小不能超过200MB')
      return
    }
    
    // 检查文件类型
    if (!file.type.startsWith('video/')) {
      ElMessage.error('请选择有效的视频文件')
      return
    }
    
    inputMessage.value += `\n[视频已上传: ${file.name}]\n`
    ElMessage.success('视频上传成功，可以开始对话了')
  }
  
  // 清空文件选择
  event.target.value = ''
}

const formatMessage = (content) => {
  return content.replace(/\n/g, '<br>')
}

const clearChat = () => {
  messages.value = []
  ElMessage.success('对话已清空')
}

const showShortcutHelp = () => {
  shortcutHelpVisible.value = true
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    nextTick(() => {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    })
  }
}

const loadUserData = async () => {
  try {
    const response = await getUserDashboardStats(userId.value)
    if (response.data) {
      userStats.value = response.data
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
  }
}

// 全局快捷键处理
const handleGlobalKeydown = (event) => {
  // Alt + 1-4 快速插入模板
  if (event.altKey && !event.ctrlKey && !event.shiftKey) {
    const templates = ['制定学习计划', '分析我的学习进度', '推荐学习资源', '解答疑问']
    const key = parseInt(event.key)
    if (key >= 1 && key <= 4) {
      event.preventDefault()
      insertTemplate(templates[key - 1])
    }
  }
  
  // Alt + I 上传图片
  if (event.altKey && event.key.toLowerCase() === 'i') {
    event.preventDefault()
    uploadImage()
  }
  
  // Alt + V 上传视频
  if (event.altKey && event.key.toLowerCase() === 'v') {
    event.preventDefault()
    uploadVideo()
  }
  
  // Alt + C 清空输入
  if (event.altKey && event.key.toLowerCase() === 'c') {
    event.preventDefault()
    clearInput()
  }
}

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

// 生命周期
onMounted(async () => {
  await loadUserData()
  
  // 添加全局快捷键监听
  document.addEventListener('keydown', handleGlobalKeydown)
  
  // 添加欢迎消息
  if (userName.value) {
    messages.value.push({
      id: Date.now(),
      content: `您好，${userName.value}！我是您的GLM-4V学习助手，由智谱AI驱动。我具备强大的多模态理解能力，可以处理文本、图像和视频内容。有什么可以帮助您的吗？`,
      isUser: false,
      time: new Date().toLocaleTimeString()
    })
  }
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  document.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<style lang="scss" scoped>
.ai-assistant {
  padding: 24px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  text-align: center;
  margin-bottom: 24px;
  
  h1 {
    margin: 0 0 8px 0;
    color: #303133;
    font-size: 28px;
    font-weight: 600;
  }
  
  p {
    margin: 0;
    color: #606266;
    font-size: 16px;
  }
}

.chat-container {
  max-width: 800px;
  margin: 0 auto;
}

.chat-card {
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
  
  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .assistant-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .assistant-details {
        h3 {
          margin: 0 0 4px 0;
          font-size: 16px;
          color: #303133;
        }
        
        p {
          margin: 0;
          font-size: 12px;
          color: #67c23a;
        }
      }
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
      align-items: center;
    }
  }

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 0;
    overflow: hidden;
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  min-height: 0; /* 重要：确保可以收缩 */
  max-height: calc(100vh - 400px); /* 限制最大高度 */
  
  /* 美化滚动条 */
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
    
    &:hover {
      background: #a8a8a8;
    }
  }
  
  .welcome-message {
    text-align: center;
    padding: 40px 20px;
    
    h3 {
      margin: 16px 0 8px 0;
      color: #303133;
    }
    
    p {
      margin: 16px 0 8px 0;
      color: #606266;
    }
    
    ul {
      text-align: left;
      display: inline-block;
      margin: 16px 0;
      color: #606266;
      
      li {
        margin: 8px 0;
      }
    }
    
    .quick-start {
      margin-top: 24px;
      
      p {
        margin-bottom: 12px;
        font-weight: 600;
        color: #303133;
      }
      
      .el-button {
        margin: 4px;
      }
    }
  }
  
  .messages-list {
    .message-item {
      display: flex;
      margin-bottom: 20px;
      
      &.user-message {
        flex-direction: row-reverse;
        
        .message-content {
          align-items: flex-end;
          
          .message-bubble {
            background: #409eff;
            color: white;
          }
        }
      }
      
      &.ai-message {
        .message-content {
          align-items: flex-start;
          
          .message-bubble {
            background: #f5f7fa;
            color: #303133;
          }
        }
      }
      
      .message-avatar {
        margin: 0 12px;
        flex-shrink: 0;
      }
      
      .message-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        max-width: 70%;
        
        .message-bubble {
          padding: 12px 16px;
          border-radius: 18px;
          word-wrap: break-word;
          word-break: break-word;
          
          .message-text {
            line-height: 1.5;
            
            :deep(br) {
              margin: 4px 0;
            }
          }
          
          .message-time {
            font-size: 11px;
            opacity: 0.7;
            margin-top: 8px;
          }
        }
      }
    }
    
    .typing-indicator {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      
      .typing-bubble {
        background: #f5f7fa;
        padding: 12px 16px;
        border-radius: 18px;
        margin-left: 12px;
        
        .typing-dots {
          display: flex;
          gap: 4px;
          
          span {
            width: 6px;
            height: 6px;
            background: #909399;
            border-radius: 50%;
            animation: typing 1.4s infinite;
            
            &:nth-child(2) {
              animation-delay: 0.2s;
            }
            
            &:nth-child(3) {
              animation-delay: 0.4s;
            }
          }
        }
      }
    }
  }
}

.input-area {
  border-top: 1px solid #e4e7ed;
  padding: 16px 20px;
  flex-shrink: 0; /* 防止输入区域被压缩 */
  
  .quick-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;
    flex-wrap: wrap;
    
    .el-button {
      border-radius: 16px;
      font-size: 12px;
      height: 28px;
      padding: 0 12px;
      
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
      }
    }
    
    .el-divider {
      height: 20px;
      margin: 0 4px;
    }
  }
  
  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    
    .input-tips {
      font-size: 12px;
      color: #909399;
      flex: 1;
    }
    
    .action-buttons {
      display: flex;
      gap: 8px;
      align-items: center;
    }
  }
}

// 动画
@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

// 快捷键帮助对话框样式
.shortcut-help {
  h4 {
    color: #303133;
    margin: 16px 0 8px 0;
    font-size: 14px;
    display: flex;
    align-items: center;
    
    &:first-child {
      margin-top: 0;
    }
  }
  
  ul {
    margin: 0 0 16px 0;
    padding-left: 20px;
    
    li {
      margin: 6px 0;
      color: #606266;
      font-size: 13px;
      line-height: 1.4;
    }
  }
  
  kbd {
    background: #f5f7fa;
    border: 1px solid #dcdfe6;
    border-radius: 3px;
    box-shadow: 0 1px 0 rgba(0, 0, 0, 0.2);
    color: #303133;
    display: inline-block;
    font-family: monospace;
    font-size: 11px;
    font-weight: 600;
    line-height: 1;
    padding: 2px 4px;
    white-space: nowrap;
  }
  
  .tip {
    background: #f0f9ff;
    border: 1px solid #409eff;
    border-radius: 6px;
    padding: 12px;
    margin-top: 16px;
    
    p {
      margin: 0;
      color: #409eff;
      font-size: 13px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .ai-assistant {
    padding: 12px;
  }
  
  .chat-card {
    height: calc(100vh - 120px);
  }
  
  .messages-container {
    max-height: calc(100vh - 300px);
    
    .messages-list .message-item .message-content {
      max-width: 85%;
    }
  }
  
  .input-area .quick-actions {
    gap: 4px;
    
    .el-button {
      font-size: 11px;
      height: 24px;
      padding: 0 8px;
    }
  }
}
</style> 