<template>
  <div class="announcement-list">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">公告管理</span>
        <el-button type="primary" @click="showDialog()">
          <el-icon><Plus /></el-icon>发布公告
        </el-button>
      </div>
      
      <el-table :data="announcements" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="clubName" label="所属社团" width="120">
          <template #default="{ row }">{{ row.clubName || '系统公告' }}</template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布者" width="100" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @change="fetchData"
      />
    </div>
    
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="form.clubId" placeholder="不选则为系统公告" style="width: 100%" clearable>
            <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import api from '../../api'
import dayjs from 'dayjs'

const userStore = useUserStore()
const announcements = ref([])
const myClubs = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  id: null,
  title: '',
  content: '',
  clubId: null
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const fetchMyClubs = async () => {
  const res = await api.get('/api/clubs/my')
  myClubs.value = res.data
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/announcements/list', {
      params: { page: page.value, size: size.value }
    })
    announcements.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const showDialog = (row = null) => {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
  } else {
    form.id = null
    form.title = ''
    form.content = ''
    form.clubId = null
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await api.put('/api/announcements', form)
      ElMessage.success('保存成功')
    } else {
      await api.post('/api/announcements', form)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定要删除该公告吗？', '提示', { type: 'warning' })
  await api.delete(`/api/announcements/${id}`)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => {
  fetchMyClubs()
  fetchData()
})
</script>
