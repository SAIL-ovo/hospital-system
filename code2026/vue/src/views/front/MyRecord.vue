<template>
  <div class="my-records-container">
    <!-- 页面标题 -->
    <h2 class="page-title">我的病历</h2>

    <!-- 刷新按钮 -->
    <div class="card" style="margin-bottom: 15px;">
      <el-button type="primary" @click="loadMyRecords">刷新病历列表</el-button>
    </div>

    <!-- 病历列表 -->
    <el-table :data="recordList" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="patientName" label="患者姓名" width="120" />
      <el-table-column prop="department" label="科室" width="120" />
      <el-table-column prop="doctorName" label="医生姓名" width="120" />
      <el-table-column prop="visitDate" label="就诊日期" width="120" />
      <el-table-column prop="diagnosis" label="诊断" min-width="200" />
      <el-table-column label="处方金额" width="120">
        <template #default="{ row }">
          ¥{{ row.totalPrice ? row.totalPrice.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="支付状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.paid ? 'success' : 'danger'">
            {{ row.paid ? '已支付' : '未支付' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
          <!-- 未支付：仅显示去支付 -->
          <el-button
              size="small"
              type="primary"
              v-if="!row.paid"
              @click="handlePay(row)"
          >
            去支付
          </el-button>
          <!-- 已支付：显示生成电子发票（领药凭证） -->
          <el-button
              size="small"
              type="success"
              v-else
              @click="generateInvoice(row)"
          >
            生成电子发票
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 病历详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="病历详情" width="70%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="患者姓名">{{ currentRecord.patientName }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ currentRecord.department }}</el-descriptions-item>
        <el-descriptions-item label="医生姓名">{{ currentRecord.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ currentRecord.visitDate }}</el-descriptions-item>
        <el-descriptions-item label="主诉" :span="2">{{ currentRecord.chiefComplaint }}</el-descriptions-item>
        <el-descriptions-item label="诊断" :span="2">{{ currentRecord.diagnosis }}</el-descriptions-item>
        <el-descriptions-item label="处方" :span="2">
          <div v-if="currentRecord.prescription" style="white-space: pre-line;">{{ currentRecord.prescription }}</div>
          <div v-else>无处方</div>
        </el-descriptions-item>
        <el-descriptions-item label="处方金额" :span="2">
          ¥{{ currentRecord.totalPrice ? currentRecord.totalPrice.toFixed(2) : '0.00' }}
        </el-descriptions-item>
        <el-descriptions-item label="支付状态" :span="2">
          <el-tag :type="currentRecord.paid ? 'success' : 'danger'">
            {{ currentRecord.paid ? '已支付' : '未支付' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 扫码支付弹窗 -->
    <el-dialog v-model="payDialogVisible" title="扫码支付" width="500px">
      <div class="pay-content">
        <p class="pay-tip">请选择支付方式并扫码支付</p>
        <el-radio-group v-model="payMethod" class="pay-method-group">
          <el-radio label="wechat">微信支付</el-radio>
          <el-radio label="alipay">支付宝支付</el-radio>
        </el-radio-group>
        <div class="qrcode-wrapper">
          <img
              :src="payMethod === 'wechat' ? wechatQrcode : alipayQrcode"
              alt="支付二维码"
              class="qrcode-img"
              @error="handleQrcodeError"
          />
        </div>
        <p class="pay-amount">支付金额：¥{{ currentRecord.totalPrice ? currentRecord.totalPrice.toFixed(2) : '0.00' }}</p>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 电子发票弹窗（领药凭证） -->
    <el-dialog v-model="invoiceDialogVisible" title="电子发票（领药凭证）" width="600px">
      <div class="invoice-content">
        <div class="invoice-header">
          <h3>社区医院电子发票</h3>
          <p class="invoice-tip">凭此发票到线下药房领取药品</p>
        </div>
        <el-descriptions :column="2" border style="margin: 20px 0;">
          <el-descriptions-item label="发票编号">
            {{ invoiceData.invoiceNo }}
          </el-descriptions-item>
          <el-descriptions-item label="患者姓名">
            {{ invoiceData.patientName }}
          </el-descriptions-item>
          <el-descriptions-item label="就诊科室">
            {{ invoiceData.department }}
          </el-descriptions-item>
          <el-descriptions-item label="主治医生">
            {{ invoiceData.doctorName }}
          </el-descriptions-item>
          <el-descriptions-item label="就诊日期">
            {{ invoiceData.visitDate }}
          </el-descriptions-item>
          <el-descriptions-item label="处方金额">
            ¥{{ invoiceData.totalPrice ? invoiceData.totalPrice.toFixed(2) : '0.00' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付状态" :span="2">
            <el-tag type="success">已支付</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开票时间" :span="2">
            {{ invoiceData.createTime ? new Date(invoiceData.createTime).toLocaleString() : '' }}
          </el-descriptions-item>
        </el-descriptions>
        <!-- 发票二维码（线下领药扫码校验） -->
        <div class="invoice-qrcode">
          <p>领药校验码</p>
          <img :src="invoiceQrcode" alt="领药二维码" class="invoice-qrcode-img" />
        </div>
      </div>
      <template #footer>
        <el-button @click="invoiceDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="printInvoice">保存/打印发票</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QRCode from 'qrcode'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

import wechatQrcode from '@/assets/imgs/wechat-pay.png'
import alipayQrcode from '@/assets/imgs/ali-pay.jpg'

const router = useRouter()

// 核心状态管理
const recordList = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const payDialogVisible = ref(false)
const invoiceDialogVisible = ref(false)
const currentRecord = ref({})
const payMethod = ref('wechat')
const invoiceData = ref({})
const invoiceQrcode = ref('')

// 页面加载获取病历
onMounted(() => {
  loadMyRecords()
})

// 加载病历列表（🔥 修复：保留真实金额，不再强制覆盖为0）
const loadMyRecords = () => {
  const userStr = localStorage.getItem('system-user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const user = JSON.parse(userStr)
  if (!user.id) {
    ElMessage.warning('用户信息异常，请重新登录')
    return
  }

  loading.value = true
  request.get('/medical-record/selectByUserId', {
    params: { userId: user.id }
  }).then(res => {
    loading.value = false
    if (res.code === '200') {
      recordList.value = res.data.map(item => ({
        ...item,
        // 🔥 关键修改：只在totalPrice为null/undefined时给0，否则保留原值
        totalPrice: item.totalPrice != null ? item.totalPrice : 0.00,
        paid: item.paid || false
      }))
    } else {
      ElMessage.warning('暂无病历记录')
      recordList.value = []
    }
  }).catch(err => {
    loading.value = false
    ElMessage.error('病历列表加载失败')
    console.error('加载病历失败：', err)
  })
}

// 查看病历详情
const viewDetail = (row) => {
  currentRecord.value = { ...row }
  detailDialogVisible.value = true
}

// 处理支付
const handlePay = (row) => {
  currentRecord.value = { ...row }
  payMethod.value = 'wechat'
  payDialogVisible.value = true
}

// 二维码加载失败处理
const handleQrcodeError = (e) => {
  e.target.src = 'https://via.placeholder.com/300/300?text=支付二维码加载失败'
}

// 确认支付（🔥 优化：只传ID，不传paid，避免后端误覆盖金额）
const confirmPay = () => {
  ElMessageBox.confirm(
      '确认已完成扫码支付？',
      '支付确认',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(() => {
    // 🔥 关键修改：只传id，让后端自己更新paid状态，避免传参错误覆盖金额
    request.post('/medical-record/pay', {
      id: currentRecord.value.id
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('支付成功！可生成电子发票领药')
        payDialogVisible.value = false
        loadMyRecords() // 刷新列表显示生成发票按钮
      } else {
        ElMessage.error(res.msg || '支付失败')
      }
    }).catch(err => {
      ElMessage.error('支付处理失败')
      console.error('支付报错：', err)
    })
  }).catch(() => {
    ElMessage.info('已取消支付')
  })
}

// 生成电子发票（核心：校验支付状态）
const generateInvoice = async (row) => {
  // 前端二次校验（防止绕过界面操作）
  if (!row.paid) {
    ElMessage.error('未支付，无法生成电子发票！')
    return
  }

  try {
    // 调用后端生成发票接口（后端再次校验支付状态）
    const res = await request.get(`/invoice/generate/${row.id}`)
    if (res.code === '200') {
      invoiceData.value = res.data
      // 生成领药二维码（包含发票号，线下扫码校验）
      invoiceQrcode.value = await QRCode.toDataURL(JSON.stringify({
        invoiceNo: invoiceData.value.invoiceNo,
        recordId: row.id,
        paid: true
      }), { width: 150, margin: 2 })
      invoiceDialogVisible.value = true
    } else {
      ElMessage.error(res.msg || '生成电子发票失败')
    }
  } catch (err) {
    ElMessage.error('生成电子发票失败，请重试')
    console.error('生成发票报错：', err)
  }
}

// 打印/保存发票
const printInvoice = () => {
  ElMessage.success('电子发票已保存，可截图/打印后到线下领药')
}
</script>

<style scoped>
/* 页面容器 */
.my-records-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  min-height: calc(100vh - 70px);
}

/* 页面标题 */
.page-title {
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

/* 支付弹窗样式 */
.pay-content {
  text-align: center;
  padding: 10px 0;
}

.pay-tip {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.pay-method-group {
  margin-bottom: 20px;
}

.pay-method-group .el-radio {
  margin: 0 15px;
}

.qrcode-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.qrcode-img {
  width: 300px;
  height: 300px;
  border: 1px solid #eee;
  padding: 10px;
  border-radius: 8px;
  object-fit: contain;
}

.pay-amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

/* 电子发票样式 */
.invoice-content {
  padding: 10px;
}

.invoice-header {
  text-align: center;
  margin-bottom: 10px;
}

.invoice-header h3 {
  color: #333;
  margin: 0;
}

.invoice-tip {
  color: #666;
  font-size: 14px;
  margin: 5px 0;
}

.invoice-qrcode {
  text-align: center;
  margin-top: 20px;
}

.invoice-qrcode p {
  color: #333;
  margin-bottom: 10px;
}

.invoice-qrcode-img {
  width: 150px;
  height: 150px;
  border: 1px solid #eee;
  padding: 5px;
  border-radius: 8px;
}
</style>