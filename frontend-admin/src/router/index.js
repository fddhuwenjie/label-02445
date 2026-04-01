import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'clubs',
        name: 'Clubs',
        component: () => import('../views/club/ClubList.vue'),
        meta: { title: '社团管理' }
      },
      {
        path: 'clubs/create',
        name: 'ClubCreate',
        component: () => import('../views/club/ClubForm.vue'),
        meta: { title: '创建社团' }
      },
      {
        path: 'clubs/:id',
        name: 'ClubDetail',
        component: () => import('../views/club/ClubDetail.vue'),
        meta: { title: '社团详情' }
      },
      {
        path: 'clubs/:id/edit',
        name: 'ClubEdit',
        component: () => import('../views/club/ClubForm.vue'),
        meta: { title: '编辑社团' }
      },
      {
        path: 'members',
        name: 'Members',
        component: () => import('../views/member/MemberList.vue'),
        meta: { title: '成员管理' }
      },
      {
        path: 'my-memberships',
        name: 'MyMemberships',
        component: () => import('../views/member/MyMemberships.vue'),
        meta: { title: '我的社团' }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('../views/activity/ActivityList.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'activities/create',
        name: 'ActivityCreate',
        component: () => import('../views/activity/ActivityForm.vue'),
        meta: { title: '创建活动' }
      },
      {
        path: 'activities/:id/edit',
        name: 'ActivityEdit',
        component: () => import('../views/activity/ActivityForm.vue'),
        meta: { title: '编辑活动' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/user/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('../views/announcement/AnnouncementList.vue'),
        meta: { title: '公告管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }
  
  if (!userStore.token) {
    next('/login')
    return
  }
  
  // 确保用户信息已加载
  if (!userStore.user) {
    userStore.fetchUserInfo().then(() => {
      // 检查角色权限
      if (to.meta.roles && !to.meta.roles.includes(userStore.user?.role)) {
        next('/dashboard')
        return
      }
      next()
    }).catch(() => {
      next('/login')
    })
    return
  }
  
  // 检查角色权限
  if (to.meta.roles && !to.meta.roles.includes(userStore.user?.role)) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
