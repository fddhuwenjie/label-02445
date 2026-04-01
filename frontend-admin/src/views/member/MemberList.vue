<template>
  <div class="member-list">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">成员管理</span>
        <el-button type="primary" @click="handleAdd" :disabled="!clubId">添加成员</el-button>
      </div>
      
      <div class="search-bar">
        <el-select v-model="clubId" placeholder="选择社团" style="width: 200px" @change="fetchData">
          <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
        </el-select>
        <el-select v-model="status" placeholder="状态" style="width: 120px" clearable @change="fetchData">
          <el-option label="待审核" :value="0" />
          <el-option label="已加入" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="已退出" :value="3" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索姓名/学号" style="width: 200px" clearable @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>
      
      <el-table :data="members" v-loading="loading">
        <el-table-column prop="clubName" label="社团" />
        <el-table-column prop="userRealName" label="姓名" />
        <el-table-column prop="userStudentId" label="学号" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''">
              {{ row.role === 'ADMIN' ? '管理员' : '成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button v-if="hasAuditPermission(row) && row.status === 0" type="success" size="small" @click="handleAudit(row.id, 1)">通过</el-button>
            <el-button v-if="hasAuditPermission(row) && row.status === 0" type="danger" size="small" @click="handleAudit(row.id, 2)">拒绝</el-button>
            <el-button v-if="hasRemovePermission(row) && row.status === 1 && row.role !== 'ADMIN'" type="danger" size="small" @click="handleRemove(row.id)">移除</el-button>
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

    <!-- 添加成员对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加成员" width="500px">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="80px">
        <el-form-item label="社团">
          <el-input :value="currentClubName" disabled />
        </el-form-item>
        <el-form-item label="用户" prop="userId">
          <el-select v-model="addForm.userId" placeholder="搜索用户（输入姓名或学号）" filterable remote :remote-method="searchUsers" style="width: 100%">
            <el-option v-for="user in userOptions" :key="user.id" :label="`${user.realName} (${user.studentId})`" :value="user.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="submitAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看成员详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="成员详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="社团">{{ viewData.clubName }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ viewData.userRealName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ viewData.userStudentId }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="viewData.role === 'ADMIN' ? 'danger' : ''">
            {{ viewData.role === 'ADMIN' ? '管理员' : '成员' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(viewData.status)">{{ getStatusText(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="加入时间">{{ formatDate(viewData.joinedAt) }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDate(viewData.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import dayjs from 'dayjs'

const myClubs = ref([])
const members = ref([])
const loading = ref(false)
const clubId = ref(null)
const status = ref(null)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 添加成员
const addDialogVisible = ref(false)
const addFormRef = ref()
const addLoading = ref(false)
const userOptions = ref([])
const addForm = reactive({ userId: null })
const addRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }]
}

// 查看详情
const viewDialogVisible = ref(false)
const viewData = ref({})

const currentClubName = computed(() => {
  const club = myClubs.value.find(c => c.id === clubId.value)
  return club ? club.name : ''
})

const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已加入', 2: '已拒绝', 3: '已退出' }
  return texts[status] || ''
}

const hasAuditPermission = (row) => {
  // 当前选择的社团就是用户管理的社团
  return true
}

const hasRemovePermission = (row) => {
  // 当前选择的社团就是用户管理的社团
  return true
}

const fetchMyClubs = async () => {
  const res = await api.get('/api/clubs/my')
  myClubs.value = res.data
  if (myClubs.value.length > 0) {
    clubId.value = myClubs.value[0].id
    fetchData()
  }
}

const fetchData = async () => {
  if (!clubId.value) {
    members.value = []
    return
  }
  loading.value = true
  try {
    const res = await api.get(`/api/memberships/club/${clubId.value}`, {
      params: { page: page.value, size: size.value, status: status.value, keyword: keyword.value }
    })
    members.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const searchUsers = async (query) => {
  if (query) {
    const res = await api.get('/api/users/search', { params: { keyword: query, size: 20 } })
    userOptions.value = res.data
  }
}

const handleAdd = () => {
  addForm.userId = null
  userOptions.value = []
  addDialogVisible.value = true
}

const submitAdd = async () => {
  await addFormRef.value.validate()
  addLoading.value = true
  try {
    await api.post('/api/memberships/add', { clubId: clubId.value, userId: addForm.userId, role: 'MEMBER' })
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    fetchData()
  } finally {
    addLoading.value = false
  }
}

const handleView = (row) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const handleAudit = async (id, status) => {
  await api.put(`/api/memberships/${id}/audit`, null, { params: { status } })
  ElMessage.success('操作成功')
  fetchData()
}

const handleRemove = async (id) => {
  await ElMessageBox.confirm('确定要移除该成员吗？', '提示', { type: 'warning' })
  await api.delete(`/api/memberships/${id}`)
  ElMessage.success('移除成功')
  fetchData()
}

onMounted(fetchMyClubs)
</script>
