<template>
  <div class="club-list">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">{{ isAdmin ? '社团管理' : '社团列表' }}</span>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>创建社团
        </el-button>
      </div>
      
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索社团名称" style="width: 200px" clearable @clear="fetchData" @keyup.enter="fetchData" />
        <el-select v-if="isAdmin" v-model="status" placeholder="状态" style="width: 120px" clearable @change="fetchData">
          <el-option label="待审核" :value="0" />
          <el-option label="正常" :value="1" />
          <el-option label="已解散" :value="2" />
        </el-select>
        <el-select v-if="!isAdmin" v-model="scope" placeholder="展示范围" style="width: 160px" clearable @change="fetchData">
          <el-option label="全部社团" value="all" />
          <el-option label="我加入的" value="joined" />
          <el-option label="我管理的" value="managed" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>
      
      <el-table :data="clubs" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="社团名称" min-width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="openDetailDialog(row.id)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" min-width="100" />
        <el-table-column prop="leaderName" label="负责人" min-width="90" />
        <el-table-column prop="memberCount" label="成员数" min-width="80" />
        <el-table-column prop="status" label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'">
              {{ row.status === 1 ? '正常' : row.status === 0 ? '待审核' : '已解散' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="150">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <div class="op-btns">
              <el-button v-if="row.status === 0 && isAdmin" type="success" size="small" @click="handleAudit(row.id, 1)">通过</el-button>
              <el-button v-if="row.status === 0 && isAdmin" type="danger" size="small" @click="handleAudit(row.id, 2)">拒绝</el-button>
              <el-button v-if="canManage(row)" type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button v-if="!canManage(row) && row.status === 1" type="primary" size="small" plain @click="openDetailDialog(row.id)">详情</el-button>
              <el-button v-if="isAdmin" type="danger" size="small" plain @click="handleDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @change="fetchData"
      />
    </div>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="formId ? '编辑社团' : '创建社团'" width="520px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="社团名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="社团类别" prop="category">
          <el-select v-model="form.category" placeholder="请选择类别" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="社团简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入社团简介" />
        </el-form-item>
        <el-form-item label="Logo URL" prop="logo">
          <el-input v-model="form.logo" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="submitForm">{{ formId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="社团详情" width="560px" destroy-on-close>
      <div v-if="detailClub" class="club-detail-content">
        <div class="detail-header">
          <div class="detail-avatar">{{ detailClub.name?.charAt(0) }}</div>
          <div class="detail-info">
            <h3>{{ detailClub.name }}</h3>
            <div class="detail-meta">
              <el-tag :type="detailClub.status === 1 ? 'success' : detailClub.status === 0 ? 'warning' : 'danger'">
                {{ detailClub.status === 1 ? '正常' : detailClub.status === 0 ? '待审核' : '已解散' }}
              </el-tag>
              <span>{{ detailClub.category }}</span>
              <span>负责人：{{ detailClub.leaderName }}</span>
              <span>{{ detailClub.memberCount }} 名成员</span>
            </div>
          </div>
        </div>
        <p class="detail-desc">{{ detailClub.description || '暂无简介' }}</p>
        <div v-if="detailClub.status === 1" class="detail-actions">
          <el-button v-if="!detailMembership && !detailChecking" type="primary" size="small" @click="applyJoin" :loading="detailApplying">申请加入</el-button>
          <el-tag v-else-if="detailMembership === 'pending'" type="warning">申请中</el-tag>
          <el-button v-else-if="detailMembership === 'member'" type="danger" size="small" plain @click="quitClub" :loading="detailQuitting">退出社团</el-button>
        </div>
      </div>
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

const clubs = ref([])
const loading = ref(false)
const keyword = ref('')
const status = ref(null)
const scope = ref('all')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const myManagedClubIds = ref([])
const categories = ref([])

const formVisible = ref(false)
const formRef = ref()
const formId = ref(null)
const formLoading = ref(false)
const form = reactive({ id: null, name: '', category: '', description: '', logo: '' })
const formRules = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择社团类别', trigger: 'change' }]
}

const detailVisible = ref(false)
const detailClub = ref(null)
const detailMembership = ref(null)
const detailChecking = ref(false)
const detailApplying = ref(false)
const detailQuitting = ref(false)

const isAdmin = computed(() => userStore.user?.role === 'ADMIN')

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const canManage = (club) => {
  if (isAdmin.value) return true
  return myManagedClubIds.value.includes(club.id)
}

const fetchCategories = async () => {
  const res = await api.get('/api/clubs/categories')
  categories.value = res.data
}

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

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, keyword: keyword.value }
    if (isAdmin.value) params.status = status.value
    else { params.status = 1; if (scope.value && scope.value !== 'all') params.scope = scope.value }
    const res = await api.get('/api/clubs/list', { params })
    clubs.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  formId.value = null
  resetForm()
  formVisible.value = true
}

const openEditDialog = async (row) => {
  formId.value = row.id
  try {
    const res = await api.get(`/api/clubs/detail/${row.id}`)
    Object.assign(form, { id: res.data.id, name: res.data.name, category: res.data.category, description: res.data.description || '', logo: res.data.logo || '' })
  } catch {
    Object.assign(form, { id: row.id, name: row.name, category: row.category, description: '', logo: '' })
  }
  formVisible.value = true
}

const resetForm = () => {
  Object.assign(form, { id: null, name: '', category: '', description: '', logo: '' })
  formRef.value?.resetFields?.()
}

const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formId.value) {
      await api.put('/api/clubs', form)
      ElMessage.success('保存成功')
    } else {
      await api.post('/api/clubs', form)
      ElMessage.success('创建成功，等待审核')
    }
    formVisible.value = false
    fetchData()
  } finally {
    formLoading.value = false
  }
}

const openDetailDialog = async (id) => {
  detailVisible.value = true
  detailClub.value = null
  detailMembership.value = null
  detailChecking.value = true
  try {
    const res = await api.get(`/api/clubs/detail/${id}`)
    detailClub.value = res.data
    const myRes = await api.get('/api/memberships/my', { params: { page: 1, size: 100 } })
    const m = myRes.data.records.find(x => x.clubId === id)
    if (m) detailMembership.value = m.status === 0 ? 'pending' : 'member'
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    detailChecking.value = false
  }
}

const applyJoin = async () => {
  if (!detailClub.value) return
  detailApplying.value = true
  try {
    await api.post(`/api/memberships/apply/${detailClub.value.id}`)
    ElMessage.success('申请已提交')
    detailMembership.value = 'pending'
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '申请失败')
  } finally {
    detailApplying.value = false
  }
}

const quitClub = async () => {
  if (!detailClub.value) return
  await ElMessageBox.confirm('确定要退出该社团吗？', '提示', { type: 'warning' })
  detailQuitting.value = true
  try {
    await api.post(`/api/memberships/quit/${detailClub.value.id}`)
    ElMessage.success('已退出')
    detailMembership.value = null
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '退出失败')
  } finally {
    detailQuitting.value = false
  }
}

const handleAudit = async (id, s) => {
  await api.put(`/api/clubs/${id}/audit`, null, { params: { status: s } })
  ElMessage.success('操作成功')
  fetchData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定要删除该社团吗？', '提示', { type: 'warning' })
  await api.delete(`/api/clubs/${id}`)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(async () => {
  fetchCategories()
  await fetchMyManagedClubs()
  fetchData()
})
</script>

<style scoped>
.op-btns { display: flex; flex-wrap: wrap; gap: 4px; }
.club-detail-content .detail-header { display: flex; gap: 16px; margin-bottom: 16px; }
.club-detail-content .detail-avatar { width: 48px; height: 48px; border-radius: 8px; background: linear-gradient(135deg, #409eff, #66b1ff); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.club-detail-content .detail-info h3 { margin: 0 0 8px 0; }
.club-detail-content .detail-meta { display: flex; flex-wrap: wrap; gap: 12px; color: #606266; font-size: 13px; }
.club-detail-content .detail-desc { color: #606266; line-height: 1.6; margin: 0 0 16px 0; }
.club-detail-content .detail-actions { margin-top: 12px; }
</style>

