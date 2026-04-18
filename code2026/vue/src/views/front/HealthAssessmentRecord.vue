<template>
  <div class="assessment-record-container">
    <el-page-header @back="goBack" content="我的自评记录">
      <el-button type="primary" @click="goToAssessment">返回自评</el-button>
    </el-page-header>

    <!-- 统计概览 -->
    <el-card class="stats-card" shadow="hover">
      <div class="stats-grid">
        <div class="stats-item">
          <span class="stats-label">累计自评次数</span>
          <span class="stats-value">{{ totalAssessmentCount }} 次</span>
        </div>
        <div class="stats-item">
          <span class="stats-label">平均得分</span>
          <span class="stats-value">{{ avgScore.toFixed(1) }} 分</span>
        </div>
        <div class="stats-item">
          <span class="stats-label">风险等级</span>
          <span class="stats-advice">{{ maxRiskLevel }}</span>
        </div>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>自评类型分布</template>
          <div class="chart-container">
            <el-empty v-if="!Object.keys(typeCount || {}).length" description="暂无自评数据" />
            <canvas v-else id="typeChart"></canvas>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>得分趋势</template>
          <div class="chart-container">
            <el-empty v-if="!scoreTrend.labels.length" description="暂无得分数据" />
            <canvas v-else id="scoreChart"></canvas>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 自评记录列表 -->
    <el-card shadow="hover" class="record-card">
      <template #header>自评记录（按时间倒序）</template>
      <el-table
          :data="assessmentRecords"
          stripe
          v-loading="loading"
          empty-text="暂无自评记录"
          style="width: 100%"
      >
        <el-table-column prop="assessmentTime" label="自评时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.assessmentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="自评类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="riskLevel" label="风险等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)">{{ getRiskName(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="suggestion" label="健康建议" min-width="250">
          <template #default="{ row }">
            <div class="suggestion-text">{{ row.suggestion || '暂无建议' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="text" @click="viewRecordDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="自评详情" width="600px">
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="自评类型">
          <el-tag :type="getTypeTagType(currentRecord.type)">{{ getTypeName(currentRecord.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="自评时间">{{ formatTime(currentRecord.assessmentTime) }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ currentRecord.totalScore }} 分</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="getRiskTagType(currentRecord.riskLevel)">{{ getRiskName(currentRecord.riskLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="健康建议">
          <div class="detail-suggestion">{{ currentRecord.suggestion || '暂无建议' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import Chart from 'chart.js/auto'

const router = useRouter()

// 状态管理
const assessmentRecords = ref([])
const detailDialogVisible = ref(false)
const currentRecord = ref({})
const loading = ref(false)
let typeChart = null
let scoreChart = null

// 统计数据（增强空值处理）
const totalAssessmentCount = computed(() => assessmentRecords.value.length || 0)

const avgScore = computed(() => {
  if (assessmentRecords.value.length === 0) return 0
  const sum = assessmentRecords.value.reduce((s, r) => s + (Number(r.totalScore) || 0), 0)
  return sum / assessmentRecords.value.length
})

const maxRiskLevel = computed(() => {
  if (assessmentRecords.value.length === 0) return '无'
  const riskOrder = { LOW: 1, MEDIUM: 2, HIGH: 3 }
  let maxLevel = 'LOW'
  assessmentRecords.value.forEach(r => {
    if (r.riskLevel && riskOrder[r.riskLevel] > riskOrder[maxLevel]) {
      maxLevel = r.riskLevel
    }
  })
  return getRiskName(maxLevel)
})

// 类型分布（增强空值处理）
const typeCount = computed(() => {
  const typeMap = {}
  assessmentRecords.value.forEach(r => {
    const type = r.type || '未知'
    typeMap[type] = (typeMap[type] || 0) + 1
  })
  return typeMap
})

// 得分趋势（修复：保留每一次自评，不再按天合并）
const scoreTrend = computed(() => {
  const timeLabels = []; // X轴：每次自评的时间（精确到分钟）
  const scoreData = [];  // Y轴：每次自评的分数

  assessmentRecords.value.forEach((r, index) => {
    try {
      const time = r.assessmentTime || r.createTime || '';
      if (!time) return;

      // 格式化时间为 "MM-DD HH:mm"，并加上序号区分同时间的多次自评
      const formattedTime = formatTimeForChart(time);
      // 如果时间相同，在标签后加上序号（如 03-17 15:02 #1）
      const label = timeLabels.includes(formattedTime)
          ? `${formattedTime} #${index + 1}`
          : formattedTime;

      timeLabels.push(label);
      scoreData.push(Number(r.totalScore) || 0);
    } catch (e) {
      console.warn('解析时间失败:', e);
    }
  });

  return { labels: timeLabels, data: scoreData };
});

// 工具方法
const formatTime = (timeStr) => {
  if (!timeStr) return '未知'
  try {
    const date = new Date(timeStr)
    if (isNaN(date.getTime())) return '未知'
    // 格式化时间为本地格式（兼容不同浏览器）
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return '未知'
  }
}

// 图表专用：格式化时间为 "MM-DD HH:mm"
const formatTimeForChart = (timeStr) => {
  if (!timeStr) return '';
  try {
    const date = new Date(timeStr);
    if (isNaN(date.getTime())) return '';
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    return `${month}-${day} ${hour}:${minute}`;
  } catch (e) {
    return '';
  }
};

const getTypeName = (type) => {
  const map = { GENERAL: '综合健康', HYPERTENSION: '高血压', DIABETES: '糖尿病' }
  return map[type] || '未知类型'
}

const getTypeTagType = (type) => {
  const map = { GENERAL: 'primary', HYPERTENSION: 'warning', DIABETES: 'danger' }
  return map[type] || 'info'
}

const getRiskName = (level) => {
  const map = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险' }
  return map[level] || '未知'
}

const getRiskTagType = (level) => {
  const map = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }
  return map[level] || 'info'
}

// 获取用户ID（增强异常处理）
const getUserId = () => {
  try {
    const userStr = localStorage.getItem('system-user')
    if (!userStr) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return null
    }
    const user = JSON.parse(userStr)
    if (!user.id) {
      ElMessage.warning('用户信息异常，缺少用户ID')
      router.push('/login')
      return null
    }
    return user.id
  } catch (e) {
    ElMessage.error('用户信息解析失败：' + e.message)
    router.push('/login')
    return null
  }
}

// 加载自评记录（增强错误处理）
const loadAssessmentRecords = () => {
  const userId = getUserId()
  if (!userId) return
  loading.value = true

  request.get(`/health/assessment/records/${userId}`)
      .then(res => {
        console.log('自评记录接口返回:', res)
        // 兼容不同的返回格式
        let data = []
        if (res.code === '200' && res.data) {
          data = Array.isArray(res.data) ? res.data : [res.data]
        }
        // 数据清洗，确保字段存在
        assessmentRecords.value = data.map(item => ({
          id: item.id || '',
          userId: item.userId || '',
          type: item.type || '',
          totalScore: item.totalScore || 0,
          riskLevel: item.riskLevel || 'LOW',
          suggestion: item.suggestion || '',
          assessmentTime: item.assessmentTime || item.createTime || '',
          ...item
        }))
        nextTick(() => renderCharts())
      })
      .catch(err => {
        console.error('加载自评记录失败:', err)
        ElMessage.error('加载失败：' + (err.message || '网络异常'))
      })
      .finally(() => {
        loading.value = false
      })
}

// 渲染图表（增强兼容性+防止内存泄漏）
const renderCharts = () => {
  // 销毁旧图表
  if (typeChart) typeChart.destroy()
  if (scoreChart) scoreChart.destroy()

  // 类型分布饼图
  const typeCtx = document.getElementById('typeChart')
  if (typeCtx && Object.keys(typeCount.value).length > 0) {
    typeChart = new Chart(typeCtx, {
      type: 'pie',
      data: {
        labels: Object.keys(typeCount.value).map(t => getTypeName(t)),
        datasets: [{
          data: Object.values(typeCount.value),
          backgroundColor: ['#36a2eb', '#ff9f40', '#ff6384', '#4bc0c0', '#9966ff'],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom'
          }
        }
      }
    })
  }

// 得分趋势图
  const scoreCtx = document.getElementById('scoreChart')
  if (scoreCtx && scoreTrend.value.labels.length > 0) {
    scoreChart = new Chart(scoreCtx, {
      type: 'line',
      data: {
        labels: scoreTrend.value.labels,
        datasets: [{
          label: '自评得分',
          data: scoreTrend.value.data,
          borderColor: '#36a2eb',
          backgroundColor: 'rgba(54, 162, 235, 0.2)',
          tension: 0.1, // 降低平滑度，让两点间直线更明显
          fill: true,
          pointRadius: 6, // 增大数据点
          pointHoverRadius: 8,
          pointBackgroundColor: '#fff',
          pointBorderColor: '#36a2eb'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: { beginAtZero: true, ticks: { stepSize: 5 } },
          x: { ticks: { maxRotation: 45, minRotation: 45 } }
        },
        plugins: { legend: { display: false } }
      }
    })
  }
}

// 查看详情
const viewRecordDetail = (row) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

// 页面跳转
const goBack = () => router.back()
const goToAssessment = () => router.push('/front/health-assessment')

// 生命周期：页面加载
onMounted(() => {
  loadAssessmentRecords()
})

// 生命周期：页面卸载（销毁图表，防止内存泄漏）
onUnmounted(() => {
  if (typeChart) typeChart.destroy()
  if (scoreChart) scoreChart.destroy()
})
</script>

<style scoped>
.assessment-record-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  min-height: calc(100vh - 70px);
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

/* 健康建议文本样式优化 */
.suggestion-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
}

.detail-suggestion {
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .el-col {
    width: 100% !important;
    margin-bottom: 20px;
    margin-bottom: 20px;
  }
  .chart-container {
    height: 250px;
  }
}
</style>