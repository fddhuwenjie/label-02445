<template>
  <div class="club-form">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">{{ isEdit ? '编辑社团' : '创建社团' }}</span>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px;">
        <el-form-item label="社团名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="社团类别" prop="category">
          <el-select v-model="form.category" placeholder="请选择类别" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="社团简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入社团简介" />
        </el-form-item>
        <el-form-item label="社团Logo" prop="logo">
          <el-input v-model="form.logo" placeholder="请输入Logo URL" />
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
const categories = ref([])
const myManagedClubIds = ref([])

const form = reactive({
  id: null,
  name: '',
  category: '',
  description: '',
  logo: ''
})

const rules = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择社团类别', trigger: 'change' }]
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

    if (isEdit.value) {
      if (!myManagedClubIds.value.includes(parseInt(route.params.id))) {
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

const fetchCategories = async () => {
  const res = await api.get('/api/clubs/categories')
  categories.value = res.data
}

const fetchClub = async () => {
  if (!route.params.id) return
  const res = await api.get(`/api/clubs/detail/${route.params.id}`)
  Object.assign(form, res.data)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await api.put('/api/clubs', form)
      ElMessage.success('保存成功')
    } else {
      await api.post('/api/clubs', form)
      ElMessage.success('创建成功，等待审核')
    }
    router.push('/clubs')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const hasPermission = await checkPermission()
  if (!hasPermission) {
    ElMessage.error('无权限操作')
    router.push('/clubs')
    return
  }
  fetchCategories()
  fetchClub()
})
</script>
