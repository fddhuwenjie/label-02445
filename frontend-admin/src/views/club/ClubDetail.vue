<template>
  <div class="club-detail">
    <div class="page-card">
      <div class="detail-page">
        <div class="detail-header">
          <div class="detail-avatar">{{ club.name?.charAt(0) }}</div>
          <div class="detail-info">
            <h2>{{ club.name }}</h2>
            <div class="detail-meta">
              <span><el-icon><Collection /></el-icon> {{ club.category }}</span>
              <span><el-icon><User /></el-icon> 负责人：{{ club.leaderName }}</span>
              <span><el-icon><UserFilled /></el-icon> {{ club.memberCount }} 名成员</span>
            </div>
          </div>
          <div class="detail-actions">
            <el-tag :type="club.status === 1 ? 'success' : club.status === 0 ? 'warning' : 'danger'" size="large">
              {{ club.status === 1 ? '正常' : club.status === 0 ? '待审核' : '已解散' }}
            </el-tag>
            <template v-if="club.status === 1">
              <el-button v-if="!membershipStatus && !checkingMembership" type="primary" @click="handleApplyJoin" :loading="applyingJoin">
                申请加入
              </el-button>
              <el-tag v-else-if="membershipStatus === 'pending'" type="warning" size="large">申请中</el-tag>
              <el-button v-else-if="membershipStatus === 'member'" type="danger" plain @click="handleQuit" :loading="quitting">
                退出社团
              </el-button>
            </template>
          </div>
        </div>
        
        <el-divider />
        
        <h3 style="margin-bottom: 12px;">社团简介</h3>
        <p style="color: #606266; line-height: 1.8;">{{ club.description || '暂无简介' }}</p>
      </div>
    </div>
    
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">社团成员</span>
      </div>
      <el-table :data="members" v-loading="membersLoading">
        <el-table-column prop="userRealName" label="姓名" />
        <el-table-column prop="userStudentId" label="学号" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'LEADER' ? 'danger' : row.role === 'ADMIN' ? 'warning' : ''">
              {{ row.role === 'LEADER' ? '负责人' : row.role === 'ADMIN' ? '管理员' : '成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'">
              {{ row.status === 1 ? '已加入' : row.status === 0 ? '待审核' : '已退出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinedAt" label="加入时间">
          <template #default="{ row }">
            {{ row.joinedAt ? formatDate(row.joinedAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column v-if="isClubAdmin" label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" @click="handleAudit(row.id, 1)">通过</el-button>
            <el-button v-if="row.status === 0" type="danger" size="small" @click="handleAudit(row.id, 2)">拒绝</el-button>
            <el-button v-if="row.status === 1 && row.role !== 'LEADER'" type="danger" size="small" @click="handleRemove(row.id)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import api from '../../api'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()
const club = ref({})
const members = ref([])
const membersLoading = ref(false)
const membershipStatus = ref(null)
const checkingMembership = ref(true)
const applyingJoin = ref(false)
const quitting = ref(false)
const isClubAdmin = ref(false)

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const fetchClub = async () => {
  const res = await api.get(`/api/clubs/detail/${route.params.id}`)
  club.value = res.data
}

const fetchMembers = async () => {
  membersLoading.value = true
  try {
    const res = await api.get(`/api/memberships/club/${route.params.id}`, {
      params: { page: 1, size: 100 }
    })
    members.value = res.data.records
  } finally {
    membersLoading.value = false
  }
}

const checkMembershipStatus = async () => {
  checkingMembership.value = true
  try {
    const res = await api.get(`/api/memberships/my`, { params: { page: 1, size: 100 } })
    const membership = res.data.records.find(m => m.clubId === Number(route.params.id))
    if (membership) {
      if (membership.status === 0) {
        membershipStatus.value = 'pending'
      } else if (membership.status === 1) {
        membershipStatus.value = 'member'
      }
    }
  } finally {
    checkingMembership.value = false
  }
}

const checkAdminPermission = async () => {
  try {
    const res = await api.get(`/api/memberships/check-admin/${route.params.id}`)
    isClubAdmin.value = res.data
  } catch {
    isClubAdmin.value = false
  }
}

const handleApplyJoin = async () => {
  applyingJoin.value = true
  try {
    await api.post(`/api/memberships/apply/${route.params.id}`)
    ElMessage.success('申请已提交，请等待审核')
    membershipStatus.value = 'pending'
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '申请失败')
  } finally {
    applyingJoin.value = false
  }
}

const handleQuit = async () => {
  await ElMessageBox.confirm('确定要退出该社团吗？', '提示', { type: 'warning' })
  quitting.value = true
  try {
    await api.post(`/api/memberships/quit/${route.params.id}`)
    ElMessage.success('已退出社团')
    membershipStatus.value = null
    fetchMembers()
    fetchClub()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '退出失败')
  } finally {
    quitting.value = false
  }
}

const handleAudit = async (id, status) => {
  await api.put(`/api/memberships/${id}/audit`, null, { params: { status } })
  ElMessage.success('操作成功')
  fetchMembers()
  fetchClub()
}

const handleRemove = async (id) => {
  await ElMessageBox.confirm('确定要移除该成员吗？', '提示', { type: 'warning' })
  await api.delete(`/api/memberships/${id}`)
  ElMessage.success('移除成功')
  fetchMembers()
  fetchClub()
}

onMounted(() => {
  fetchClub()
  fetchMembers()
  checkMembershipStatus()
  checkAdminPermission()
})
</script>

<style scoped>
.detail-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
