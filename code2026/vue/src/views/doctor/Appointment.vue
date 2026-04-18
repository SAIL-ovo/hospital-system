<template>
  <div class="appointment-container">
    <!-- 查询和重置区域 -->
    <div class="card" style="margin-bottom: 15px;">
      <el-input
          v-model="data.patientName"
          style="width: 300px; margin-right: 10px"
          placeholder="请输入患者姓名查询"
          :prefix-icon="Search"
      />
      <el-button type="success" @click="loadPendingAppointments">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <!-- 标签页：修改文案为“已确认预约”，匹配实际功能 -->
    <el-tabs v-model="activeTab" type="card" style="margin-bottom: 15px;">
      <el-tab-pane label="待确认预约" name="pending"></el-tab-pane>
      <el-tab-pane label="已确认预约" name="confirmed"></el-tab-pane>
    </el-tabs>

    <!-- 待确认预约列表（保留分页） -->
    <el-table v-if="activeTab === 'pending'" :data="data.tableData" style="width: 100%" stripe>
      <el-table-column prop="userId" label="患者ID" width="80" />
      <el-table-column prop="patientName" label="患者姓名" width="120" />
      <el-table-column prop="departmentName" label="科室" width="120" />
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column prop="time" label="时段" width="120" />
      <el-table-column label="就诊状态" width="100">
        <template #default="scope">
          <el-tag type="warning" size="small">未就诊</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" type="success" @click="handleConfirm(scope.row.id)">确认就诊</el-button>
          <el-button size="small" type="danger" @click="handleCancel(scope.row.id)">取消预约</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 已确认预约列表：补充空数据提示 -->
    <el-table v-if="activeTab === 'confirmed'" :data="todayAppointments" style="width: 100%" stripe>
      <el-table-column prop="userId" label="患者ID" width="80" />
      <el-table-column prop="patientName" label="患者姓名" width="120" />
      <el-table-column prop="departmentName" label="科室" width="120" />
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column prop="time" label="时段" width="120" />
      <el-table-column label="就诊状态" width="100">
        <template #default="scope">
          <el-tag type="success" size="small">已确认</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" type="success" @click="handleComplete(scope.row.id)">完成就诊</el-button>
          <el-button size="small" type="primary" @click="createRecord(scope.row)">生成病历</el-button>
          <el-button size="small" type="danger" @click="handleCancel(scope.row.id)">取消预约</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 已确认列表空数据提示 -->
    <div v-if="activeTab === 'confirmed' && todayAppointments.length === 0" style="text-align: center; padding: 40px; color: #999;">
      暂无已确认预约
    </div>

    <!-- 分页组件：仅待确认标签页显示 -->
    <div v-if="activeTab === 'pending'" class="card" style="margin-top: 10px;">
      <el-pagination
          background
          layout="total, prev, pager, next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @current-change="loadPendingAppointments"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import router from '@/router'

// 状态管理：标签页改为 confirmed
const activeTab = ref('pending')
const todayAppointments = ref([])
const doctor = JSON.parse(localStorage.getItem('system-user') || '{}')

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  patientName: null,
})

// 科室名称映射
const deptMap = reactive({
  1: '内科',
  2: '外科',
  3: '儿科',
  4: '皮肤科',
  5: '妇科',
  6: '口腔科'
})

// 加载待确认预约（带分页+姓名查询）
const loadPendingAppointments = () => {
  if (!doctor.id) return;
  request.get(`/appointment/selectPage`, {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      doctorId: doctor.id,
      status: '待确认',
      patientName: data.patientName,
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list?.map(item => ({
        ...item,
        departmentName: deptMap[item.departmentId] || '未知科室'
      })) || [];
      data.total = res.data?.total || 0;
    }
  }).catch(err => {
    ElMessage.error('待确认预约加载失败');
  })
}

// 加载已确认预约（复用原接口，已改后端逻辑）
const loadConfirmedAppointments = () => {
  if (!doctor.id) return;
  console.log('请求医生ID:', doctor.id);
  request.get(`/appointment/today/${doctor.id}`).then(res => {
    console.log('已确认预约接口返回:', res);
    if (res.code === '200') {
      todayAppointments.value = res.data?.map(item => ({
        ...item,
        departmentName: deptMap[item.departmentId] || '未知科室'
      })) || [];
      console.log('处理后的数据:', todayAppointments.value);
    }
  }).catch(err => {
    ElMessage.error('已确认预约加载失败');
  })
}

// 重置查询条件
const reset = () => {
  data.patientName = null;
  data.pageNum = 1;
  loadPendingAppointments();
}

// 确认就诊
const handleConfirm = (id) => {
  request.put(`/appointment/status/${id}`, { status: '已确认' }).then(res => {
    if (res.code === '200') {
      ElMessage.success('已确认就诊');
      loadPendingAppointments(); // 刷新待确认
      loadConfirmedAppointments(); // 刷新已确认
    } else {
      ElMessage.error(res.msg || '操作失败');
    }
  }).catch(err => {
    ElMessage.error('确认失败');
  })
}

// 完成就诊
const handleComplete = (id) => {
  request.put(`/appointment/status/${id}`, { status: '已完成' }).then(res => {
    if (res.code === '200') {
      ElMessage.success('已完成就诊');
      loadConfirmedAppointments(); // 刷新已确认（完成后会被移除）
    } else {
      ElMessage.error(res.msg || '操作失败');
    }
  }).catch(err => {
    ElMessage.error('完成失败');
  })
}

// 取消预约
const handleCancel = (id) => {
  request.put(`/appointment/status/${id}`, { status: '已取消' }).then(res => {
    if (res.code === '200') {
      ElMessage.success('已取消预约');
      loadPendingAppointments();
      loadConfirmedAppointments();
    } else {
      ElMessage.error(res.msg || '操作失败');
    }
  }).catch(err => {
    ElMessage.error('取消失败');
  })
}

// 页面加载初始化
onMounted(() => {
  if (!doctor.id) {
    ElMessage.warning('请先登录医生账号');
    return;
  }
  loadPendingAppointments();
  loadConfirmedAppointments();
})

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'pending') {
    loadPendingAppointments();
  } else if (newTab === 'confirmed') {
    loadConfirmedAppointments();
  }
})

//生成电子病历
const createRecord = (row) => {
  // 先检查是否已有病历
  request.get(`/medical-record/appointment/${row.id}`).then(res => {
    if (res.data) {
      ElMessage.warning('该预约已生成过病历，可在电子病历页面编辑')
      // 跳转到电子病历页面
      router.push('/doctor/record')
      return
    }
    // 跳转到电子病历页面，并通过路由参数预填充数据
    router.push({
      path: '/doctor/record',
      query: {
        appointmentId: row.id,
        patientId: row.userId,
        patientName: row.patientName,
        department: row.departmentName,
        visitDate: row.date
      }
    })
  })
}

</script>

<style scoped>
.appointment-container {
  padding: 20px;
}
.el-table {
  --el-table-header-text-color: #2d3748;
  --el-table-row-hover-bg-color: #f5f8ff;
}
:deep(.el-tabs__item.is-active) {
  background-color: rgba(145, 145, 145, 0.5) !important;
}
</style>