<template>
  <div class="front-layout">
    <!-- 顶部导航栏（现代化磨砂玻璃效果） -->
    <header class="header">
      <div class="header-container">
        <div class="logo-wrapper" @click="goToHome" style="cursor: pointer;">
          <img src="@/assets/imgs/logo.png" alt="xx区xx街-社区医院" class="logo-img" />
          <h1
              class="logo-text"
              @click="goToHome"
              style="cursor: pointer;"
              title="回到首页"
          >
            xx区xx街-社区医院
          </h1>
        </div>
        <div class="user-menu">
          <el-dropdown trigger="click" placement="bottom-end">
            <div class="user-avatar-wrapper">
              <img
                  class="user-avatar"
                  :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                  alt="用户头像"
              />
              <span class="user-name">{{ data.user.name || 'THG' }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="dropdown-menu">
                <el-dropdown-item @click="router.push('/front/person')" class="dropdown-item">
                  <el-icon><User /></el-icon>
                  <span>个人信息</span>
                </el-dropdown-item>
                <el-dropdown-item @click="goToMyRecords" class="dropdown-item">
                  <el-icon><Document /></el-icon>
                  <span>我的病历</span>
                </el-dropdown-item>
                <el-dropdown-item @click="openMyAppointment" class="dropdown-item">
                  <el-icon><Notification /></el-icon>
                  <span>我的预约</span>
                </el-dropdown-item>
                <el-dropdown-item @click="goToHealthRecord" class="dropdown-item">
                  <el-icon><DataLine /></el-icon>
                  <span>健康档案</span>
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/password')" class="dropdown-item">
                  <el-icon><Lock /></el-icon>
                  <span>修改密码</span>
                </el-dropdown-item>
                <el-dropdown-item @click="logout" class="dropdown-item danger">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view @updateUser="updateUser" />
    </main>

    <!-- 我的预约弹窗 -->
    <el-dialog
        title="我的预约"
        v-model="showMyAppointmentDialog"
        width="900px"
        :before-close="handleCloseAppointmentDialog"
        class="appointment-dialog"
    >
      <div class="appointment-manage">
        <!-- 状态筛选 -->
        <el-select v-model="filterStatus" placeholder="筛选预约状态" class="status-filter">
          <el-option label="全部" value="" />
          <el-option label="待就诊" value="待确认" />
          <el-option label="已完成" value="已确认" />
          <el-option label="已取消" value="已取消" />
        </el-select>

        <!-- 预约列表 -->
        <el-table
            :data="filteredAppointments"
            border
            empty-text="暂无预约记录"
            class="appointment-table"
            style="width: 100%"
        >
          <el-table-column prop="date" label="就诊日期" width="120" />
          <el-table-column prop="time" label="就诊时段" width="130" />
          <el-table-column prop="departmentName" label="就诊科室" width="120" />
          <el-table-column prop="doctorName" label="就诊医生" width="120" />
          <el-table-column label="预约状态" width="100">
            <template #default="scope">
              <el-tag
                  :type="scope.row.status === '待确认' ? 'warning' :
                        scope.row.status === '已确认' ? 'success' : 'danger'"
              >
                {{
                  scope.row.status === '待确认' ? '待就诊' :
                      scope.row.status === '已确认' ? '已完成' : '已取消'
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="预约时间" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button
                  v-if="scope.row.status === '待确认'"
                  type="text"
                  @click="cancelAppointment(scope.row.id)"
                  :disabled="isAppointmentExpired(scope.row)"
              >
                {{ isAppointmentExpired(scope.row) ? '已过期' : '取消预约' }}
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { ArrowDown, User, Lock, SwitchButton, Document, Notification, DataLine } from "@element-plus/icons-vue";
import { useRouter } from 'vue-router'
import request from '@/utils/request'; // 导入请求工具

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
});
const router = useRouter()

// 我的预约相关响应式数据
const showMyAppointmentDialog = ref(false);
const filterStatus = ref('');
const myAppointmentList = reactive([]);
const departmentList = reactive([
  { id: 1, name: '内科', icon: '🩺' },
  { id: 2, name: '外科', icon: '🏥' },
  { id: 3, name: '儿科', icon: '🧑' },
  { id: 4, name: '皮肤科', icon: '💊' },
  { id: 5, name: '妇科', icon: '🩸' },
  { id: 6, name: '口腔科', icon: '🦷' }
]);

// 未登录跳转
if (!data.user?.id) {
  ElMessage.error('请登录');
  router.push('/login');
}

// 跳转到我的病历
const goToMyRecords = () => {
  router.push('/front/my-records')
};

//新增健康档案跳转方法
const goToHealthRecord = () => {
  router.push('/front/health-record') // 对应你配置的健康档案路由路径
};

// 新增跳转到首页的方法
const goToHome = () => {
  router.push('/front/home');
};

// 退出登录
const logout = () => {
  localStorage.removeItem('system-user');
  router.push('/login');
  ElMessage.success('退出成功');
};

// 更新用户信息
const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}');
};

// 打开我的预约弹窗
const openMyAppointment = () => {
  // 校验登录状态
  if (!data.user.id) {
    ElMessage.warning('请先登录后查看预约记录');
    router.push('/login');
    return;
  }
  showMyAppointmentDialog.value = true;
  loadMyAppointments(); // 加载当前用户的预约数据
};

// 关闭弹窗时重置筛选条件
const handleCloseAppointmentDialog = () => {
  showMyAppointmentDialog.value = false;
  filterStatus.value = '';
};

// 加载当前用户的预约列表
const loadMyAppointments = () => {
  // 清空原有数据
  myAppointmentList.length = 0;

  // 调用后端接口查询当前用户的预约
  request.get('/appointment/selectAll', {
    params: {
      userId: data.user.id // 按用户ID筛选
    }
  }).then(res => {
    if (res.code === '200' && res.data) {
      // 格式化预约数据
      res.data.forEach(item => {
        myAppointmentList.push({
          id: item.id,
          date: item.date,
          time: item.time,
          departmentId: item.departmentId,
          // 匹配科室名称
          departmentName: departmentList.find(dept => dept.id === item.departmentId)?.name || '未知科室',
          doctorId: item.doctorId,
          doctorName: item.doctorName || '未知医生', // 直接用后端返回的医生姓名
          status: item.status || '待确认',
          createTime: formatDate(new Date()) // 实际项目中可从后端返回createTime
        });
      });
    }
  }).catch(err => {
    console.error('加载预约列表失败：', err);
    ElMessage.error('加载预约记录失败，请稍后重试');
    // 测试数据（接口异常时兜底）
    myAppointmentList.push({
      id: 1,
      date: '2026-03-20',
      time: '08:00-10:00',
      departmentId: 1,
      departmentName: '内科',
      doctorName: '赵文静',
      status: '待确认',
      createTime: '2026-03-15 10:20:30'
    });
  });
};

// 日期格式化工具
const formatDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 筛选后的预约列表
const filteredAppointments = computed(() => {
  if (!filterStatus.value) return myAppointmentList;
  return myAppointmentList.filter(item => item.status === filterStatus.value);
});

// 判断预约是否过期（不可取消）
const isAppointmentExpired = (appointment) => {
  if (!appointment.date) return true;

  const today = new Date().toDateString();
  const appointDate = new Date(appointment.date).toDateString();

  // 日期已过 → 过期
  if (new Date(appointment.date) < new Date().setHours(0, 0, 0, 0)) {
    return true;
  }

  // 当天但时段已过 → 过期
  if (appointDate === today) {
    const timeEnd = appointment.time?.split('-')[1];
    if (!timeEnd) return true;

    const [endHour, endMinute] = timeEnd.split(':').map(Number);
    const now = new Date();
    const endTime = new Date();
    endTime.setHours(endHour, endMinute, 0);

    return now > endTime;
  }

  return false;
};

// 取消预约（调用后端更新状态接口）
const cancelAppointment = (id) => {
  ElMessageBox.confirm(
      '确定要取消该预约吗？取消后将无法恢复，请谨慎操作！',
      '取消预约',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(() => {
    // 调用后端更新状态接口
    request.put('/appointment/updateStatus', {
      id: id,
      status: '已取消'
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('预约已成功取消');
        loadMyAppointments(); // 刷新预约列表
      } else {
        ElMessage.error(res.msg || '取消预约失败，请稍后重试');
      }
    }).catch(err => {
      console.error('取消预约失败：', err);
      ElMessage.error('取消预约失败，请稍后重试');
    });
  });
};

// 修复后端接口路径（适配你的Controller）
const updateStatus = (id, status) => {
  return request.put(`/appointment/status/${id}`, { status });
};
</script>

<style scoped>
/* 全局布局 */
.front-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 头部导航 */
.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #e8f4f8;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 999;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 70px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 医院logo */
.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-img {
  width: 42px;
  height: 42px;
  object-fit: contain;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #01948a;
  margin: 0;
}

/* 用户菜单 */
.user-menu {
  display: flex;
  align-items: center;
}

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 30px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.user-avatar-wrapper:hover {
  background-color: #f5f8ff;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #f0f7ff;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.dropdown-icon {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s ease;
}

.el-dropdown:focus-within .dropdown-icon {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.dropdown-menu {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  min-width: 160px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 14px;
  color: #333;
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background-color: #f5f8ff;
  color: #1f6feb;
}

.dropdown-item.danger {
  color: #e53e3e;
}

.dropdown-item.danger:hover {
  background-color: #fff5f5;
  color: #e53e3e;
}

/* 主内容区 */
.main-content {
  flex: 1;
  background-color: #f6f6f6;
}

/* 我的预约弹窗样式 */
.appointment-dialog {
  --el-dialog-border-radius: 12px;
}

.status-filter {
  margin-bottom: 20px;
  width: 200px;
}

.appointment-table {
  width: 100%;
  --el-table-row-hover-bg-color: #f5f8ff;
}

/* 适配小屏幕 */
@media (max-width: 768px) {
  .appointment-dialog {
    width: 95% !important;
  }
  .appointment-table {
    font-size: 12px;
  }
}
</style>