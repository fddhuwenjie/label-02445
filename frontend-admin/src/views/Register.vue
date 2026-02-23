<template>
  <div class="register-page">
    <!-- 左侧装饰区域 -->
    <div class="register-left">
      <div class="decoration">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
      <div class="brand">
        <div class="logo">
          <span class="logo-icon">🎓</span>
        </div>
        <h1>加入我们</h1>
        <p>开启精彩的社团之旅，发现志同道合的伙伴</p>
      </div>
      <div class="benefits">
        <div class="benefit-item">
          <span class="check-icon">✓</span>
          <span>免费创建和加入社团</span>
        </div>
        <div class="benefit-item">
          <span class="check-icon">✓</span>
          <span>参与丰富多彩的活动</span>
        </div>
        <div class="benefit-item">
          <span class="check-icon">✓</span>
          <span>结识志同道合的朋友</span>
        </div>
        <div class="benefit-item">
          <span class="check-icon">✓</span>
          <span>提升个人综合能力</span>
        </div>
      </div>
    </div>

    <!-- 右侧注册区域 -->
    <div class="register-right">
      <div class="register-box">
        <div class="register-header">
          <h2>创建账号</h2>
          <p>填写以下信息完成注册</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
          <div class="form-row">
            <el-form-item prop="username" class="form-item-half">
              <el-input v-model="form.username" placeholder="用户名" size="large" class="custom-input">
                <template #prefix><el-icon class="input-icon"><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="realName" class="form-item-half">
              <el-input v-model="form.realName" placeholder="真实姓名" size="large" class="custom-input">
                <template #prefix><el-icon class="input-icon"><UserFilled /></el-icon></template>
              </el-input>
            </el-form-item>
          </div>

          <el-form-item prop="studentId">
            <el-input v-model="form.studentId" placeholder="学号" size="large" class="custom-input">
              <template #prefix><el-icon class="input-icon"><Postcard /></el-icon></template>
            </el-input>
          </el-form-item>

          <div class="form-row">
            <el-form-item prop="password" class="form-item-half">
              <el-input v-model="form.password" type="password" placeholder="密码" size="large" class="custom-input" show-password>
                <template #prefix><el-icon class="input-icon"><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword" class="form-item-half">
              <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" class="custom-input" show-password>
                <template #prefix><el-icon class="input-icon"><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item prop="email" class="form-item-half">
              <el-input v-model="form.email" placeholder="邮箱（选填）" size="large" class="custom-input">
                <template #prefix><el-icon class="input-icon"><Message /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="phone" class="form-item-half">
              <el-input v-model="form.phone" placeholder="手机号（选填）" size="large" class="custom-input">
                <template #prefix><el-icon class="input-icon"><Phone /></el-icon></template>
              </el-input>
            </el-form-item>
          </div>

          <el-form-item>
            <el-button type="primary" class="register-btn" :loading="loading" @click="handleRegister">
              <span v-if="!loading">立即注册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, Lock, Message, Phone, Postcard } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  studentId: '',
  email: '',
  phone: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }]
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await api.post('/api/auth/register', {
      username: form.username,
      password: form.password,
      realName: form.realName,
      studentId: form.studentId,
      email: form.email,
      phone: form.phone
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  background: #f8fafc;
}

/* 左侧装饰区域 */
.register-left {
  flex: 1;
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
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
  right: -100px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: 100px;
  left: -50px;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 40%;
  right: 20%;
}

.brand {
  position: relative;
  z-index: 1;
  color: #fff;
  margin-bottom: 48px;
}

.logo-icon {
  font-size: 64px;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
}

.brand h1 {
  font-size: 36px;
  font-weight: 700;
  margin: 24px 0 12px;
  letter-spacing: 2px;
}

.brand p {
  font-size: 16px;
  opacity: 0.9;
  line-height: 1.6;
}

.benefits {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  font-size: 15px;
}

.check-icon {
  width: 24px;
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

/* 右侧注册区域 */
.register-right {
  width: 580px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.register-box {
  width: 100%;
  max-width: 480px;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.register-header p {
  color: #64748b;
  font-size: 15px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-item-half {
  flex: 1;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 4px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e2e8f0;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #764ba2;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #764ba2;
  box-shadow: 0 0 0 3px rgba(118, 75, 162, 0.15);
}

.custom-input :deep(.el-input__inner) {
  height: 44px;
  font-size: 14px;
}

.input-icon {
  font-size: 16px;
  color: #94a3b8;
}

.register-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  border: none;
  transition: all 0.3s;
  letter-spacing: 4px;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(118, 75, 162, 0.4);
}

.register-footer {
  text-align: center;
  color: #64748b;
  font-size: 14px;
  margin-top: 24px;
}

.login-link {
  color: #764ba2;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.3s;
}

.login-link:hover {
  color: #667eea;
}

/* 响应式 */
@media (max-width: 1024px) {
  .register-left {
    display: none;
  }
  
  .register-right {
    width: 100%;
  }
}
</style>
