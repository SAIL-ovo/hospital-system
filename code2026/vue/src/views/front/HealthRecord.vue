<template>
  <div class="health-record-container">
    <h2 class="page-title">我的健康档案</h2>

    <!-- 统计概览 -->
    <el-card class="stats-card" shadow="hover">
      <div class="stats-grid">
        <div class="stats-item">
          <span class="stats-label">累计就诊次数</span>
          <span class="stats-value">{{ totalVisitCount }} 次</span>
        </div>
        <div class="stats-item">
          <span class="stats-label">累计花费</span>
          <span class="stats-value">¥{{ formatPrice(totalCost) }}</span>
        </div>
        <div class="stats-item">
          <span class="stats-label">健康建议</span>
          <span class="stats-advice">{{ healthAdvice }}</span>
        </div>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>科室就诊分布</template>
          <div class="chart-container">
            <el-empty v-if="!Object.keys(deptCount || {}).length" description="暂无科室数据" />
            <canvas v-else id="deptChart"></canvas>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>就诊金额花费趋势</template>
          <div class="chart-container">
            <el-empty v-if="!dailyCost.labels.length" description="暂无消费数据" />
            <canvas v-else id="costChart"></canvas>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 就诊记录 -->
    <el-card shadow="hover" class="record-card">
      <template #header>就诊记录（按时间倒序）</template>
      <el-table
          :data="healthRecords"
          stripe
          v-loading="loading"
          empty-text="暂无就诊记录"
          style="width: 100%"
      >
        <el-table-column prop="visitDate" label="就诊日期" width="120">
          <template #default="{ row }">
            {{ row.visitDate || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="department" label="就诊科室" width="120" />
        <el-table-column prop="doctorName" label="接诊医生" width="120" />
        <el-table-column prop="chiefComplaint" label="主诉" min-width="150" />
        <el-table-column prop="diagnosis" label="诊断结果" min-width="150" />
        <el-table-column prop="totalPrice" label="花费金额" width="120">
          <template #default="{ row }">
            ¥{{ formatPrice(row.totalPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="paid" label="支付状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.paid ? 'success' : 'danger'">
              {{ row.paid ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="text" @click="viewRecordDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 病历详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="病历详情" width="800px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="患者姓名" :span="2">{{ currentRecord.patientName || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="就诊科室">{{ currentRecord.department || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="接诊医生">{{ currentRecord.doctorName || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ currentRecord.visitDate || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="花费金额">¥{{ formatPrice(currentRecord.totalPrice) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="currentRecord.paid ? 'success' : 'danger'">
            {{ currentRecord.paid ? '已支付' : '未支付' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="主诉" :span="2">{{ currentRecord.chiefComplaint || '无' }}</el-descriptions-item>
        <el-descriptions-item label="现病史" :span="2">
          <div class="detail-text">{{ currentRecord.historyOfPresentIllness || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="既往史" :span="2">
          <div class="detail-text">{{ currentRecord.pastHistory || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="体格检查" :span="2">
          <div class="detail-text">{{ currentRecord.physicalExamination || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="诊断结果" :span="2">
          <div class="detail-text">{{ currentRecord.diagnosis || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="治疗方案" :span="2">
          <div class="detail-text">{{ currentRecord.treatmentPlan || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="处方内容" :span="2">
          <div class="detail-text">{{ currentRecord.prescription || '无' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import Chart from 'chart.js/auto'

const router = useRouter()

// 状态管理
const healthRecords = ref([])
const detailDialogVisible = ref(false)
const currentRecord = ref({})
const loading = ref(false)
let deptChart = null
let costChart = null

// 计算统计数据
const totalVisitCount = computed(() => healthRecords.value.length || 0)

const totalCost = computed(() =>
    healthRecords.value.reduce((sum, record) => sum + (Number(record.totalPrice) || 0), 0)
)

const healthAdvice = computed(() => {
  const count = totalVisitCount.value
  return count === 0 ? "暂无建议" :
      count >= 3 ? '近半年就诊次数较多，建议定期做全面体检' : '就诊记录正常'
})

// 计算科室分布
const deptCount = computed(() => {
  const deptMap = {}
  healthRecords.value.forEach(record => {
    const dept = record.department || '未知'
    deptMap[dept] = (deptMap[dept] || 0) + 1
  })
  return deptMap
})

// 🔥 按天统计花费（核心修改）
const dailyCost = computed(() => {
  const dayMap = {}
  healthRecords.value.forEach(record => {
    const dateStr = record.visitDate || record.createTime || ''
    const match = dateStr.match(/(\d{4}-\d{2}-\d{2})/)
    const day = match ? match[1] : '未知日期'
    const cost = Number(record.totalPrice) || 0
    dayMap[day] = (dayMap[day] || 0) + cost
  })
  // 按日期排序
  const sortedDays = Object.keys(dayMap).sort()
  const sortedCosts = sortedDays.map(day => dayMap[day])
  return { labels: sortedDays, data: sortedCosts }
})

// 格式化金额
const formatPrice = (price) => {
  if (!price && price !== 0) return '0.00'
  const num = Number(price)
  return isNaN(num) ? '0.00' : num.toFixed(2)
}

// 获取用户ID
const getUserId = () => {
  const userStr = localStorage.getItem('system-user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return null
  }
  try {
    return JSON.parse(userStr).id
  } catch (e) {
    ElMessage.error('用户信息解析失败')
    return null
  }
}

// 加载数据
const loadHealthData = () => {
  const userId = getUserId();
  if (!userId) return;
  loading.value = true;

  Promise.all([
    request.get('/medical-record/health/record', { params: { userId } })
  ]).then(([recordRes]) => {
    console.log('📦 原始病历数据:', recordRes.data);

    const rawRecords = Array.isArray(recordRes.data) ? recordRes.data.flat(Infinity) : [recordRes.data];
    healthRecords.value = rawRecords.map(record => ({
      visitDate: record.visitDate || record.createTime || '未知',
      department: record.department || '未知',
      doctorName: record.doctorName || (record.doctor && record.doctor.name) || '未知',
      chiefComplaint: record.chiefComplaint || '无',
      diagnosis: record.diagnosis || '无',
      totalPrice: record.totalPrice || 0,
      paid: record.paid || false,
      ...record
    }));

    nextTick(() => renderCharts());
  }).catch(err => {
    console.error('数据加载失败:', err);
    ElMessage.error('加载失败：' + err.message);
  }).finally(() => {
    loading.value = false;
  });
};

// 渲染图表
const renderCharts = () => {
  const data = { deptCount: deptCount.value, dailyCost: dailyCost.value }
  console.log('🎨 渲染图表用数据:', data)

  // 科室分布饼图
  const deptCtx = document.getElementById('deptChart')
  if (deptCtx && data.deptCount) {
    if (deptChart) deptChart.destroy()
    deptChart = new Chart(deptCtx, {
      type: 'pie',
      data: {
        labels: Object.keys(data.deptCount),
        datasets: [{
          data: Object.values(data.deptCount),
          backgroundColor: ['#36a2eb', '#ff6384', '#ff9f40']
        }]
      },
      options: { responsive: true, maintainAspectRatio: false }
    })
  }

  // 每日花费趋势图
  const costCtx = document.getElementById('costChart')
  if (costCtx && data.dailyCost.labels.length > 0) {
    if (costChart) costChart.destroy()
    costChart = new Chart(costCtx, {
      type: 'line',
      data: {
        labels: data.dailyCost.labels,
        datasets: [{
          label: '每日花费（元）',
          data: data.dailyCost.data,
          borderColor: '#36a2eb',
          backgroundColor: 'rgba(54, 162, 235, 0.2)',
          tension: 0.1,
          pointRadius: 4,
          pointBackgroundColor: '#36a2eb'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: { beginAtZero: true, ticks: { callback: v => `¥${v}` } },
          x: {
            title: { display: true, text: '日期' },
            ticks: { maxRotation: 45, minRotation: 45 }
          }
        },
        plugins: { legend: { position: 'top' } }
      }
    })
  }
}

// 查看详情
const viewRecordDetail = (row) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

// 页面加载
onMounted(() => {
  loadHealthData()
})
</script>

<style scoped>
.health-record-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  min-height: calc(100vh - 70px);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.stats-card {
  padding: 20px;
  margin-bottom: 20px;
}

.stats-grid {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 20px;
}

.stats-item {
  text-align: center;
  flex: 1;
  min-width: 150px;
}

.stats-label {
  font-size: 14px;
  color: #666;
  display: block;
  margin-bottom: 5px;
}

.stats-value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.stats-advice {
  font-size: 14px;
  color: #409EFF;
  font-weight: 500;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-card {
  margin-top: 20px;
}

.detail-text {
  white-space: pre-wrap;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .el-col {
    width: 100% !important;
    margin-bottom: 20px;
  }
}
</style>