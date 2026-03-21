<template>
  <div class="profile">
    <el-row :gutter="20">
      <el-col :span="8">
        <div class="page-card profile-left">
          <div class="profile-content">
            <div class="profile-avatar">{{ userStore.user?.realName?.charAt(0) || 'U' }}</div>
            <h3 class="profile-name">{{ userStore.user?.realName }}</h3>
            <p class="profile-username">{{ userStore.user?.username }}</p>
            <el-tag class="profile-role" :type="userStore.user?.role === 'ADMIN' ? 'danger' : ''">
              {{ userStore.user?.role === 'ADMIN' ? '管理员' : '学生' }}
            </el-tag>
          </div>
        </div>
      </el-col>
      
      <el-col :span="16">
        <div class="page-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="info">
              <el-form ref="infoFormRef" :model="infoForm" label-width="80px" style="max-width: 500px;">
                <el-form-item label="用户名">
                  <el-input v-model="infoForm.username" disabled />
                </el-form-item>
                <el-form-item label="学号">
                  <el-input v-model="infoForm.studentId" disabled />
                </el-form-item>
                <el-form-item label="真实姓名">
                  <el-input v-model="infoForm.realName" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="infoForm.email" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="infoForm.phone" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="infoLoading" @click="handleUpdateInfo">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="修改密码" name="password">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 500px;">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="我的社团" name="clubs">
              <el-table :data="myMemberships">
                <el-table-column prop="clubName" label="社团名称" />
                <el-table-column prop="role" label="角色">
                  <template #default="{ row }">
                    <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''">
                      {{ row.role === 'ADMIN' ? '管理员' : '成员' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="joinedAt" label="加入时间">
                  <template #default="{ row }">{{ formatDate(row.joinedAt) }}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import api from '../../api'
import dayjs from 'dayjs'

const userStore = useUserStore()
const activeTab = ref('info')
const infoFormRef = ref()
const pwdFormRef = ref()
const infoLoading = ref(false)
const pwdLoading = ref(false)
const myMemberships = ref([])

const infoForm = reactive({
  username: '',
  studentId: '',
  realName: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD') : '-'

const fetchUserInfo = async () => {
  const user = await userStore.fetchUserInfo()
  Object.assign(infoForm, user)
}

const fetchMyMemberships = async () => {
  const res = await api.get('/api/memberships/my', { params: { page: 1, size: 100, status: 1 } })
  myMemberships.value = res.data.records
}

const handleUpdateInfo = async () => {
  infoLoading.value = true
  try {
    await api.put('/api/users/info', {
      realName: infoForm.realName,
      email: infoForm.email,
      phone: infoForm.phone
    })
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
  } finally {
    infoLoading.value = false
  }
}

const handleChangePassword = async () => {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await api.put('/api/users/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } finally {
    pwdLoading.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
  fetchMyMemberships()
})
</script>

<style scoped>
.profile-left {
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-content {
  text-align: center;
  padding: 20px 0;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32px;
  margin: 0 auto;
}

.profile-name {
  margin: 16px 0 8px;
}

.profile-username {
  color: #909399;
}

.profile-role {
  margin-top: 12px;
}
</style>
