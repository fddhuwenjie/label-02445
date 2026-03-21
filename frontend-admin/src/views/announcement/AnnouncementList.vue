<template>
  <div class="announcement-list">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">{{ isAdmin ? '公告管理' : '公告列表' }}</span>
        <el-button v-if="canPublish" type="primary" @click="showDialog()">
          <el-icon><Plus /></el-icon>发布公告
        </el-button>
      </div>
      
      <el-table :data="announcements" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="showDetail(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="clubName" label="所属社团" width="120">
          <template #default="{ row }">{{ row.clubName || '系统公告' }}</template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布者" width="100" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="showDetail(row)">查看</el-button>
            <el-button v-if="canManage(row)" type="warning" size="small" link @click="showDialog(row)">编辑</el-button>
            <el-button v-if="canManage(row)" type="danger" size="small" link @click="handleDelete(row.id)">删除</el-button>
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

    <!-- 查看公告详情弹窗 -->
    <el-dialog v-model="detailVisible" title="公告详情" width="600px" destroy-on-close>
      <div v-if="detailData" class="announcement-detail">
        <h3>{{ detailData.title }}</h3>
        <div class="detail-meta">
          <el-tag v-if="detailData.clubName" size="small">{{ detailData.clubName }}</el-tag>
          <el-tag v-else size="small" type="warning">系统公告</el-tag>
          <span>发布者：{{ detailData.publisherName }}</span>
          <span>{{ formatDate(detailData.createdAt) }}</span>
        </div>
        <el-divider />
        <div class="detail-content">{{ detailData.content }}</div>
      </div>
    </el-dialog>
    
    <!-- 发布/编辑公告弹窗 -->
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import api from '../../api'
import dayjs from 'dayjs'

const userStore = useUserStore()
const announcements = ref([])
const myClubs = ref([])
const myManagedClubIds = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const submitLoading = ref(false)

const detailVisible = ref(false)
const detailData = ref(null)

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

const isAdmin = computed(() => userStore.user?.role === 'ADMIN')
const canPublish = computed(() => isAdmin.value || myManagedClubIds.value.length > 0)

const canManage = (row) => {
  if (isAdmin.value) return true
  // 社团公告：社团管理员可以管理
  if (row.clubId && myManagedClubIds.value.includes(row.clubId)) return true
  return false
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const fetchMyManagedClubs = async () => {
  if (isAdmin.value) return
  try {
    const res = await api.get('/api/memberships/my', { params: { page: 1, size: 100, status: 1 } })
    myManagedClubIds.value = res.data.records
      .filter(m => m.role === 'ADMIN')
      .map(m => m.clubId)
  } catch {
    myManagedClubIds.value = []
  }
}

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

const showDetail = (row) => {
  detailData.value = row
  detailVisible.value = true
}

const showDialog = (row = null) => {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { id: row.id, title: row.title, content: row.content, clubId: row.clubId })
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

onMounted(async () => {
  await fetchMyManagedClubs()
  if (canPublish.value) fetchMyClubs()
  fetchData()
})
</script>

<style scoped>
.announcement-detail h3 { margin: 0 0 12px 0; }
.announcement-detail .detail-meta { display: flex; align-items: center; gap: 12px; color: #909399; font-size: 13px; }
.announcement-detail .detail-content { color: #303133; line-height: 1.8; white-space: pre-wrap; }
</style>
