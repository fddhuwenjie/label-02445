<template>
  <div class="activity-form">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">{{ isEdit ? '编辑活动' : '创建活动' }}</span>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px;">
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="form.clubId" placeholder="请选择社团" style="width: 100%" :disabled="isEdit">
            <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="人数限制" prop="maxParticipants">
          <el-input-number v-model="form.maxParticipants" :min="0" placeholder="0表示不限" />
          <span style="margin-left: 10px; color: #909399;">0表示不限制人数</span>
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入活动描述" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">{{ isEdit ? '保存' : '创建' }}</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import api from '../../api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isEdit = computed(() => !!route.params.id)
const formRef = ref()
const loading = ref(false)
const checkingPermission = ref(true)
const myClubs = ref([])
const myManagedClubIds = ref([])

const form = reactive({
  id: null,
  clubId: null,
  title: '',
  location: '',
  startTime: null,
  endTime: null,
  maxParticipants: 0,
  description: ''
})

const rules = {
  clubId: [{ required: true, message: '请选择社团', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const isAdmin = computed(() => userStore.user?.role === 'ADMIN')

const checkPermission = async () => {
  checkingPermission.value = true
  try {
    if (isAdmin.value) {
      return true
    }

    const res = await api.get('/api/memberships/my', { params: { page: 1, size: 100, status: 1 } })
    myManagedClubIds.value = res.data.records
      .filter(m => m.role === 'ADMIN')
      .map(m => m.clubId)

    if (myManagedClubIds.value.length === 0) {
      return false
    }

    if (isEdit.value) {
      const activityRes = await api.get(`/api/activities/detail/${route.params.id}`)
      if (!myManagedClubIds.value.includes(activityRes.data.clubId)) {
        return false
      }
    }

    return true
  } catch {
    return false
  } finally {
    checkingPermission.value = false
  }
}

const fetchMyClubs = async () => {
  const res = await api.get('/api/clubs/my')
  myClubs.value = res.data
}

const fetchActivity = async () => {
  if (!route.params.id) return
  const res = await api.get(`/api/activities/detail/${route.params.id}`)
  Object.assign(form, res.data)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await api.put('/api/activities', form)
      ElMessage.success('保存成功')
    } else {
      await api.post('/api/activities', form)
      ElMessage.success('创建成功')
    }
    router.push('/activities')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const hasPermission = await checkPermission()
  if (!hasPermission) {
    ElMessage.error('无权限操作')
    router.push('/activities')
    return
  }
  fetchMyClubs()
  fetchActivity()
})
</script>
