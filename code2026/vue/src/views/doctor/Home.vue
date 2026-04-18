<template>
  <div class="doctor-home">
    <!-- 数据概览卡片 -->
    <div class="data-cards">
      <div class="card-item" v-for="item in dataCards" :key="item.key">
        <div class="card-icon" :style="{ backgroundColor: item.color }">
          <el-icon :size="24" :color="'#fff'">
            <component :is="item.icon"></component>
          </el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">{{ item.value }}</div>
          <div class="card-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <div class="action-buttons">
        <el-button type="primary" icon="Calendar" @click="router.push('/doctor/appointment')">
          今日预约
        </el-button>
        <el-button type="success" icon="Document" @click="router.push('/doctor/record')">
          病历管理
        </el-button>
        <el-button type="warning" icon="ShoppingCart" @click="toPrescription">
          处方开具
        </el-button>
        <el-button style="background-color: #997bff; color: white" icon="Message" @click="router.push('/doctor/consultation')">
          问诊管理
        </el-button>
        <el-button type="info" icon="User" @click="router.push('/doctor/person')">
          个人信息
        </el-button>
      </div>
    </div>

    <!-- 今日待诊预约 -->
    <div class="recent-updates">
      <div class="update-card">
        <h3 class="section-title">今日待诊预约</h3>
        <el-table :data="todayAppointments" stripe border empty-text="暂无数据">
          <el-table-column prop="patientName" label="患者姓名" width="120" />
          <el-table-column prop="departmentName" label="科室" width="120" />
          <el-table-column prop="date" label="日期" width="150" />
          <el-table-column prop="time" label="时段" width="100" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="primary" size="small" @click="startConsult(scope.row)">
                开始就诊
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 预约分页组件 -->
        <div style="margin-top: 15px; text-align: right;">
          <el-pagination
              background
              layout="total, prev, pager, next"
              v-model:page-size="appointmentPage.pageSize"
              v-model:current-page="appointmentPage.pageNum"
              :total="appointmentPage.total"
              @current-change="loadTodayAppointment"
          />
        </div>
      </div>

      <!-- 药品库存 -->
      <div class="update-card">
        <h3 class="section-title">药品库存</h3>
        <el-table :data="medicineList" stripe border empty-text="暂无数据">
          <el-table-column prop="name" label="药品名" width="160" />
          <el-table-column prop="store" label="库存" width="100">
            <template #default="scope">
              <el-tag type="danger" v-if="scope.row.store < 10">{{ scope.row.store }}</el-tag>
              <span v-else>{{ scope.row.store }}</span>
            </template>
          </el-table-column>
          <!-- 规格/单位列：显示description并清理HTML标签 -->
          <el-table-column label="规格/单位" width="220">
            <template #default="scope">
              {{ cleanDescription(scope.row) }}
            </template>
          </el-table-column>
        </el-table>
        <!-- 药品分页组件 -->
        <div style="margin-top: 15px; text-align: right;">
          <el-pagination
              background
              layout="total, prev, pager, next"
              v-model:page-size="medicinePage.pageSize"
              v-model:current-page="medicinePage.pageNum"
              :total="medicinePage.total"
              @current-change="loadMedicine"
          />
        </div>
      </div>
    </div>

    <!-- 就诊弹窗 -->
    <el-dialog title="就诊" v-model="dialogVisible" width="700px">
      <el-form label-width="100px">
        <el-form-item label="患者">
          <span style="font-weight:bold">{{ currentRow?.patientName }}</span>
        </el-form-item>
        <el-form-item label="症状">
          <el-input v-model="form.symptoms" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="诊断">
          <el-input v-model="form.diagnosis" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="医嘱">
          <el-input v-model="form.advice" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord">保存病历</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Warning, Document ,Message,ChatDotRound} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 路由实例
const router = useRouter()

// 卡片数据
const dataCards = reactive([
  { key: 'today', label: '今日待诊', value: 0, icon: Calendar, color: '#409EFF' },
  { key: 'consultWait', label: '待回复问诊', value: 0, icon: Message, color: '#997bff' },
  { key: 'medicine', label: '低库存药品', value: 0, icon: Warning, color: '#F56C6C' },
  { key: 'record', label: '今日病历', value: 0, icon: Document, color: '#67C23A' }
])

const setCardValue = (key, value) => {
  const card = dataCards.find(item => item.key === key)
  if (card) {
    card.value = value
  }
}

// 核心数据
const todayAppointments = ref([])
const medicineList = ref([])
const dialogVisible = ref(false)
const currentRow = ref(null)
const doctor = JSON.parse(localStorage.getItem('system-user') || '{}')

const form = ref({
  symptoms: '',
  diagnosis: '',
  advice: ''
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

// 分页配置（必须在调用函数前定义）
const appointmentPage = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0
})

const medicinePage = reactive({
  pageNum: 1,
  pageSize: 8,
  total: 0
})

// 清理description中的HTML标签（核心修复：方法内处理正则）
const cleanDescription = (row) => {
  if (!row || !row.description) return '无'
  // 使用构造函数创建正则，避免模板解析错误
  const htmlTagReg = new RegExp('<[^>]+>', 'g')
  return row.description.replace(htmlTagReg, '')
}

// 加载今日待确认预约（带分页）
const loadTodayAppointment = () => {
  if (!doctor.id) {
    ElMessage.warning('请先登录医生账号');
    return;
  }
  request.get(`/appointment/selectPage`, {
    params: {
      pageNum: appointmentPage.pageNum,
      pageSize: appointmentPage.pageSize,
      doctorId: doctor.id,
      status: '待确认',
      date: new Date().toISOString().split('T')[0]
    }
  }).then(res => {
    if (res.code === '200') {
      todayAppointments.value = res.data?.list?.map(item => ({
        ...item,
        departmentName: deptMap[item.departmentId] || '未知科室'
      })) || [];
      appointmentPage.total = res.data?.total || 0;
      dataCards[0].value = appointmentPage.total;
    }
  }).catch(err => {
    ElMessage.error('今日待诊预约加载失败');
    console.error(err);
  })
}

// 加载药品库存（带分页）
const loadMedicine = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: medicinePage.pageNum,
      pageSize: medicinePage.pageSize
    }
  }).then(res => {
    if (res.code === '200') {
      medicineList.value = res.data?.list || [];
      medicinePage.total = res.data?.total || 0;
      // 移除这里的低库存统计，避免覆盖全量数据
    }
  }).catch(err => {
    ElMessage.error('药品库存加载失败');
  })
}

// 加载所有药品，用于统计低库存总数
const loadAllMedicine = () => {
  request.get('/goods/selectAll') // 假设后端有这个接口，返回所有药品
      .then(res => {
        if (res.code === '200') {
          const allMedicine = res.data || [];
          const lowStockCount = allMedicine.filter(i => i.store < 10).length;
          setCardValue('medicine', lowStockCount);
        }
      })
      .catch(err => {
        console.error('全量药品加载失败', err);
      })
}

// 加载今日病历总数（直接从病历列表过滤）
const loadTodayRecordCount = () => {
  if (!doctor.id) {
    ElMessage.warning('请先登录医生账号');
    return;
  }

  // 1. 生成今日日期，格式为 YYYY-MM-DD
  const today = new Date().toISOString().split('T')[0];

  // 2. 调用病历列表接口，获取所有病历
  request.get(`/medical-record/doctor/${doctor.id}`)
      .then(res => {
        if (res.code === '200') {
          const allRecords = res.data || [];
          // 3. 过滤出 visit_date 等于今天的病历
          const todayRecords = allRecords.filter(record => record.visitDate === today);
          // 4. 更新卡片数字
          setCardValue('record', todayRecords.length);
        }
      })
      .catch(err => {
        console.error('今日病历统计失败', err);
        ElMessage.error('今日病历统计失败');
      });
}

//加载待回复问诊数（调用后端接口）
const loadWaitConsultCount = () => {
  if (!doctor.id) return;
  request.get(`/consultation/doctor/countWait/${doctor.id}`).then(res => {
    if (res.code === '200') {
      dataCards.find(item => item.key === 'consultWait').value = res.data || 0;
    }
  }).catch(err => {
    console.error('加载待回复问诊数失败：', err);
  });
};

// 页面初始化
onMounted(() => {
  if (!doctor.id) {
    ElMessage.warning('请先登录医生账号');
    router.push('/login');
    return;
  }
  loadTodayAppointment();
  loadMedicine();
  loadAllMedicine();
  loadTodayRecordCount();
  loadWaitConsultCount();
  // 路由监听时同步刷新
  watch(
      () => router.currentRoute.path,
      (newPath) => {
        if (newPath === '/doctor/home') {
          loadTodayAppointment();
          loadMedicine();
          loadTodayRecordCount();
          loadWaitConsultCount(); // 新增
        }
      }
  );
});

// 开始就诊：跳转至已确认预约标签页
const startConsult = (row) => {
  router.push({
    path: '/doctor/appointment',
    query: { tab: 'confirmed' }
  })
}

// 保存病历
const saveRecord = () => {
  if (!currentRow.value?.id) {
    ElMessage.error('预约信息缺失，无法保存病历');
    return;
  }
  request.post('/doctor/record/add', {
    appointmentId: currentRow.value.id,
    patientId: currentRow.value.userId,
    doctorId: doctor.id,
    chiefComplaint: form.value.symptoms,
    diagnosis: form.value.diagnosis,
    treatmentPlan: form.value.advice
  }).then(() => {
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    loadTodayAppointment();
    loadTodayRecordCount();
  }).catch(err => {
    ElMessage.error('保存失败');
    console.error(err);
  })
}

// 处方开具占位
const toPrescription = () => {
  ElMessage.info('前往处方开具页面');
}
</script>

<style scoped>
.doctor-home {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.data-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}
.card-item {
  flex: 1;
  min-width: 180px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  gap: 15px;
}
.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}
.card-label {
  font-size: 14px;
  color: #666;
}
.quick-actions {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}
.section-title {
  font-size: 16px;
  font-weight: bold;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}
.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 10px;
}
.recent-updates {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.update-card {
  flex: 1;
  min-width: 400px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
</style>
