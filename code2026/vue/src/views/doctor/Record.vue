<template>
  <div class="record-container">
    <div class="card" style="margin-bottom: 15px;">
      <el-button type="primary" @click="refreshRecords">刷新病历列表</el-button>
    </div>

    <el-table :data="recordList" style="width: 100%" stripe>
      <el-table-column prop="patientName" label="患者姓名" width="120" />
      <el-table-column prop="department" label="科室" width="120" />
      <el-table-column prop="doctorName" label="医生姓名" width="120" />
      <el-table-column prop="visitDate" label="就诊日期" width="120" />
      <el-table-column prop="diagnosis" label="诊断" min-width="200" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" type="success" @click="editRecord(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 查看/编辑病历弹窗 -->
    <el-dialog v-model="dialogVisible" title="电子病历" width="70%" :before-close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者姓名">
              <el-input v-model="formData.patientName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室">
              <el-input v-model="formData.department" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="就诊日期">
              <el-date-picker v-model="formData.visitDate" type="date" value-format="YYYY-MM-DD" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生姓名">
              <el-input v-model="formData.doctorName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="主诉" prop="chiefComplaint">
          <el-input v-model="formData.chiefComplaint" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="现病史" prop="historyOfPresentIllness">
          <el-input v-model="formData.historyOfPresentIllness" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="既往史" prop="pastHistory">
          <el-input v-model="formData.pastHistory" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="体格检查" prop="physicalExamination">
          <el-input v-model="formData.physicalExamination" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="诊断" prop="diagnosis">
          <el-input v-model="formData.diagnosis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="治疗方案" prop="treatmentPlan">
          <el-input v-model="formData.treatmentPlan" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="处方" prop="prescription">
          <el-input v-model="formData.prescription" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="openGoodsDialog">开具药品</el-button>
        <el-button type="primary" @click="saveRecord">保存</el-button>
      </template>
    </el-dialog>
  </div>

  <!-- 开具药品弹窗 -->
  <el-dialog v-model="goodsDialogVisible" title="开具药品" width="60%">
    <div class="card" style="margin-bottom: 15px;">
      <el-input v-model="goodsQuery.name" style="width: 300px; margin-right: 10px" placeholder="请输入药品名称" />
      <el-button type="success" @click="loadGoods">查询</el-button>
      <el-button type="info" @click="resetGoodsQuery">重置</el-button>
    </div>
    <el-table :data="goodsList" style="width: 100%" stripe>
      <el-table-column prop="name" label="药品名称" width="180" />
      <el-table-column prop="price" label="单价" width="80" />
      <el-table-column prop="store" label="库存" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-input-number v-model="scope.row.num" :min="1" :max="scope.row.store" size="small" style="width: 100px" />
            <el-button size="small" type="primary" @click="addPrescription(scope.row)">添加</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <!-- 新增分页组件 -->
    <div class="card" style="margin-top: 15px;">
      <el-pagination
          background
          layout="total, prev, pager, next"
          v-model:page-size="goodsPage.pageSize"
          v-model:current-page="goodsPage.pageNum"
          :total="goodsPage.total"
          @current-change="loadGoods"
      />
    </div>

    <div style="margin-top: 15px;">
      <h4>已选药品：</h4>
      <el-table :data="prescriptionList" style="width: 100%" stripe>
        <el-table-column prop="name" label="药品名称" width="180" />
        <el-table-column prop="num" label="数量" width="80" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" type="danger" @click="removePrescription(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 已选药品总价统计 -->
      <div style="margin-top: 10px; text-align: right; font-size: 16px; font-weight: bold;">
        药品总价：¥{{ totalPrice.toFixed(2) }}
      </div>
    </div>
    <template #footer>
      <el-button @click="goodsDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmPrescription">确认</el-button>
    </template>
  </el-dialog>

</template>

<script setup>
import { ref, reactive, onMounted, watch, computed} from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'

const route = useRoute()
const doctor = JSON.parse(localStorage.getItem('system-user') || '{}')
const recordList = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const formData = reactive({
  id: null,
  appointmentId: null,
  patientId: null,
  doctorId: doctor.id,
  patientName: '',
  doctorName: doctor.name,
  department: '',
  visitDate: '',
  chiefComplaint: '',
  historyOfPresentIllness: '',
  pastHistory: '',
  physicalExamination: '',
  diagnosis: '',
  treatmentPlan: '',
  prescription: ''
})
const rules = {
  chiefComplaint: [{ required: true, message: '请输入主诉', trigger: 'blur' }],
  diagnosis: [{ required: true, message: '请输入诊断', trigger: 'blur' }]
}

const refreshRecords = () => {
  if (!doctor.id) {
    ElMessage.warning('请先登录医生账号')
    return
  }
  request.get(`/medical-record/doctor/${doctor.id}`).then(res => {
    if (res.code === '200') {
      recordList.value = res.data || []
    }
  }).catch(() => {
    ElMessage.error('病历列表加载失败')
  })
}

const viewRecord = (row) => {
  Object.assign(formData, row)
  dialogVisible.value = true
}

const editRecord = (row) => {
  Object.assign(formData, row)
  dialogVisible.value = true
}


const resetForm = () => {
  Object.keys(formData).forEach(key => {
    if (key !== 'doctorId' && key !== 'doctorName') {
      formData[key] = null
    }
  })
}

onMounted(() => {
  refreshRecords()
})

// 监听路由参数，生成新病历时预填充数据
watch(() => route.query, (query) => {
  if (query.appointmentId) {
    Object.assign(formData, {
      appointmentId: Number(query.appointmentId),
      patientId: Number(query.patientId),
      patientName: query.patientName,
      department: query.department,
      visitDate: query.visitDate
    })
    dialogVisible.value = true // 自动打开病历弹窗
  }
}, { immediate: true })

//开具药品相关方法
const goodsDialogVisible = ref(false)
const goodsList = ref([])
const prescriptionList = ref([])
const goodsQuery = reactive({
  name: ''
})
// 开具药品的分页状态
const goodsPage = reactive({
  pageNum: 1,
  pageSize: 8,
  total: 0
})

// 加载药品列表（分页）
const loadGoods = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: goodsPage.pageNum,
      pageSize: goodsPage.pageSize,
      name: goodsQuery.name,
      status: '上架'
    }
  }).then(res => {
    goodsList.value = res.data?.list?.map(item => ({ ...item, num: 1 })) || []
    goodsPage.total = res.data?.total || 0
  })
}

// 重置药品查询
const resetGoodsQuery = () => {
  goodsQuery.name = ''
  goodsPage.pageNum = 1
  loadGoods()
}

// 添加到处方
const addPrescription = (row) => {
  const exist = prescriptionList.value.find(item => item.id === row.id)
  if (exist) {
    exist.num = row.num
  } else {
    prescriptionList.value.push({ ...row })
  }
}

// 从处方移除
const removePrescription = (id) => {
  prescriptionList.value = prescriptionList.value.filter(item => item.id !== id)
}

// 确认处方
const confirmPrescription = () => {
  // 把处方信息拼接到病历的 prescription 字段
  const prescriptionText = prescriptionList.value
      .map(item => `${item.name} × ${item.num}`)
      .join('\n')
  formData.prescription = prescriptionText
  goodsDialogVisible.value = false
}

// 打开药品弹窗
const openGoodsDialog = () => {
  loadGoods()
  goodsDialogVisible.value = true
}

const saveRecord = () => {
  formRef.value?.validate(valid => {
    if (valid) {
      //把处方总价赋值给 formData，一起提交
      formData.totalPrice = totalPrice.value
      // 打印总价，确认计算是否正确
      console.log('处方总价:', totalPrice.value)
      formData.totalPrice = totalPrice.value
      //初始化支付状态为未支付
      formData.paid = false

      const requestFn = formData.id
          ? request.put('/medical-record/update', formData)
          : request.post('/medical-record/add', formData)

      requestFn.then(res => {
        if (res.code === '200') {
          const updatePromises = prescriptionList.value.map(item => {
            return request.put(`/goods/deductStore`, {
              id: item.id,
              num: item.num
            })
          })

          Promise.all(updatePromises).then(() => {
            ElMessage.success('保存成功，库存和销量已更新！')
            dialogVisible.value = false
            refreshRecords()
            prescriptionList.value = []
          }).catch(() => {
            ElMessage.error('病历保存成功，但库存/销量更新失败！')
          })
        } else {
          ElMessage.error(res.msg || '保存失败')
        }
      }).catch(() => {
        ElMessage.error('保存请求失败')
      })
    }
  })
}

// 计算处方总价
const totalPrice = computed(() => {
  return prescriptionList.value.reduce((sum, item) => {
    return sum + (item.price * item.num)
  }, 0)
})
</script>

<style scoped>
.record-container {
  padding: 20px;
}
</style>