<template>
  <div class="login-page">
    <!-- 左侧装饰区域 -->
    <div class="login-left">
      <div class="decoration">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
      <div class="brand">
        <div class="logo">
          <span class="logo-icon">🎓</span>
        </div>
        <h1>学生社团管理系统</h1>
        <p>连接兴趣，汇聚热爱，共创精彩校园生活</p>
      </div>
      <div class="features">
        <div class="feature-item">
          <div class="feature-icon">📋</div>
          <div class="feature-text">
            <h4>社团管理</h4>
            <p>轻松创建和管理社团</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">👥</div>
          <div class="feature-text">
            <h4>成员协作</h4>
            <p>高效的成员管理体验</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">🎉</div>
          <div class="feature-text">
            <h4>活动组织</h4>
            <p>便捷的活动发布与报名</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账号</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="用户名" 
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码" 
              size="large"
              class="custom-input"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              class="login-btn" 
              :loading="loading" 
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>

        <div class="demo-accounts">
          <p class="demo-title">演示账号</p>
          <div class="demo-list">
            <span class="demo-item" @click="fillDemo('admin', 'admin123')">管理员</span>
            <span class="demo-item" @click="fillDemo('leader', 'leader123')">负责人</span>
            <span class="demo-item" @click="fillDemo('student', 'student123')">学生</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const fillDemo = (username, password) => {
  form.username = username
  form.password = password
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: #f8fafc;
}

/* 左侧装饰区域 */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.decoration .circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: 100px;
  right: -50px;
}

.circle-3 {
  width: 150px;
  height: 150px;
  bottom: -50px;
  left: 30%;
}

.brand {
  position: relative;
  z-index: 1;
  color: #fff;
  margin-bottom: 60px;
}

.logo {
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 64px;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
}

.brand h1 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: 2px;
}

.brand p {
  font-size: 16px;
  opacity: 0.9;
  line-height: 1.6;
}

.features {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: transform 0.3s, background 0.3s;
}

.feature-item:hover {
  transform: translateX(8px);
  background: rgba(255, 255, 255, 0.2);
}

.feature-icon {
  font-size: 28px;
}

.feature-text h4 {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.feature-text p {
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}

/* 右侧登录区域 */
.login-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-box {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.login-header p {
  color: #64748b;
  font-size: 15px;
}

.login-form {
  margin-bottom: 24px;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 4px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e2e8f0;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.custom-input :deep(.el-input__inner) {
  height: 48px;
  font-size: 15px;
}

.input-icon {
  font-size: 18px;
  color: #94a3b8;
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
  letter-spacing: 4px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  color: #64748b;
  font-size: 14px;
  margin-bottom: 32px;
}

.register-link {
  color: #667eea;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.3s;
}

.register-link:hover {
  color: #764ba2;
}

.demo-accounts {
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.demo-title {
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  margin-bottom: 12px;
}

.demo-list {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.demo-item {
  padding: 8px 16px;
  background: #f1f5f9;
  border-radius: 8px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s;
}

.demo-item:hover {
  background: #667eea;
  color: #fff;
}

/* 响应式 */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
  }
}
</style>
