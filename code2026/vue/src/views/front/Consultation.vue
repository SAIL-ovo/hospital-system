<template>
  <div class="consultation-container">
    <div class="page-header">
      <h2 class="page-title">在线问诊</h2>
      <p class="page-desc">发起新问诊或查看历史问诊记录</p>
    </div>

    <!-- 标签页：整合 发起问诊 + 问诊记录 -->
    <el-tabs v-model="activeTab" type="card" class="consult-tabs">
      <el-tab-pane label="发起新问诊" name="add">
        <el-card class="consult-form-card" shadow="hover">
          <el-form :model="form" ref="formRef" :rules="formRules" label-width="80px">
            <el-form-item label="问诊医生" prop="doctorId">
              <el-select v-model="form.doctorId" placeholder="请选择问诊医生" style="width: 100%">
                <el-option v-for="doc in doctorList" :key="doc.id"
                           :label="`${doc.name} - ${doc.department} ${doc.title}`" :value="doc.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="症状描述" prop="content">
              <el-input v-model="form.content" type="textarea" rows="6"
                        placeholder="请详细描述症状、时间、病史等" />
            </el-form-item>

            <el-form-item label="上传图片（可选）">
              <el-upload
                  :action="uploadUrl"
                  :limit="3"
                  :on-success="handleUploadSuccess"
                  :on-exceed="handleUploadExceed"
                  list-type="picture-card"
                  :file-list="fileList"
                  name="file"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <p class="upload-tip">最多上传3张图片，单张≤5MB</p>
            </el-form-item>

            <el-form-item class="submit-btn-wrapper">
              <el-button type="primary" size="large" @click="submitConsult">提交问诊</el-button>
              <el-button size="large" @click="handleResetForm">重置表单</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 标签2：我的问诊记录（查询+列表+分页+详情） -->
      <el-tab-pane label="我的问诊记录" name="list">
        <div class="search-bar">
          <el-form :inline="true" :model="searchForm">
            <el-form-item label="接诊医生">
              <el-select v-model="searchForm.doctorId" clearable style="width: 200px">
                <el-option v-for="doc in doctorList" :key="doc.id"
                           :label="`${doc.name} - ${doc.department}`" :value="doc.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="loadList">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无问诊记录">
          <el-table-column prop="id" label="问诊ID" width="80" align="center" />
          <el-table-column label="接诊医生" width="180" align="center">
            <template #default="scope">{{ getDoctorName(scope.row.doctorId) }}</template>
          </el-table-column>
          <el-table-column prop="content" label="问诊内容" min-width="250" show-overflow-tooltip />
          <el-table-column prop="createTime" label="发起时间" width="180" align="center" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag type="warning" v-if="scope.row.status === 0">待回复</el-tag>
              <el-tag type="success" v-if="scope.row.status === 1">已回复</el-tag>
              <el-tag type="info" v-if="scope.row.status === 2">已关闭</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button size="small" type="primary" @click="viewDetail(scope.row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :current-page="pageNum"
              :page-size="pageSize"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 问诊详情弹窗 -->
    <el-dialog v-model="detailVisible" title="问诊详情" width="70%">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="问诊ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="接诊医生">{{ getDoctorName(currentDetail.doctorId) }}</el-descriptions-item>
        <el-descriptions-item label="问诊内容"><div class="text-box">{{ currentDetail.content }}</div></el-descriptions-item>
        <el-descriptions-item label="医生回复" v-if="currentDetail.replyContent"><div class="text-box reply">{{ currentDetail.replyContent }}</div></el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="success" v-if="currentDetail.status === 1">已回复</el-tag>
          <el-tag type="warning" v-else-if="currentDetail.status === 0">待回复</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
// 激活标签
const activeTab = ref('list')

// ==================== 发起问诊相关 ====================
const formRef = ref(null)
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
const form = reactive({ doctorId: '', content: '', userId: '', imgUrls: '' })
const formRules = reactive({
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  content: [{ required: true, message: '请描述症状', trigger: 'blur' }]
})
const doctorList = ref([])
const fileList = ref([])

// ==================== 问诊记录相关 ====================
const loading = ref(false)
const searchForm = reactive({ doctorId: '', status: '' })
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const detailVisible = ref(false)
const currentDetail = ref({})

// 初始化
onMounted(() => {
  checkLogin()
  loadDoctorList()
  loadList()
})

// 登录校验
const checkLogin = () => {
  const user = localStorage.getItem('system-user')
  if (!user) { ElMessage.warning('请先登录'); router.push('/login'); return }
  form.userId = JSON.parse(user).id
}

// 加载医生列表
const loadDoctorList = async () => {
  const res = await request.get('/doctor/selectPage', { params: { pageNum:1, pageSize:100 } })
  if (res.code === '200') doctorList.value = res.data.list.filter(i=>i.status===1)
}

// 提交问诊
const submitConsult = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    await request.post('/consultation/add', form)
    ElMessage.success('问诊提交成功！')
    // 重置表单 + 切换到记录页 + 刷新列表
    handleResetForm()
    activeTab.value = 'list'
    loadList()
  })
}

// 重置问诊表单
const handleResetForm = () => {
  form.doctorId = ''
  form.content = ''
  form.imgUrls = ''
  fileList.value = []
}

// 图片上传
const handleUploadSuccess = (res) => {
  if (res.code === '200') form.imgUrls = form.imgUrls ? `${form.imgUrls},${res.data}` : res.data
}
const handleUploadExceed = () => ElMessage.warning('最多上传3张')

// ==================== 问诊列表 ====================
const loadList = async () => {
  loading.value = true
  const user = JSON.parse(localStorage.getItem('system-user'))
  const params = { ...searchForm, pageNum: pageNum.value, pageSize: pageSize.value, userId: user.id }
  const res = await request.get('/consultation/user/selectPage', { params })
  if (res.code === '200') {
    tableData.value = res.data.list
    total.value = res.data.total
  }
  loading.value = false
}

// 重置查询
const resetSearch = () => {
  searchForm.doctorId = ''
  searchForm.status = ''
  pageNum.value = 1
  loadList()
}

// 医生名称转换
const getDoctorName = (id) => {
  const doc = doctorList.value.find(i => i.id === id)
  return doc ? `${doc.name}-${doc.department}` : '未知医生'
}

// 查看详情
const viewDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

// 分页
const handleSizeChange = (val) => { pageSize.value = val; loadList() }
const handleCurrentChange = (val) => { pageNum.value = val; loadList() }
</script>

<style scoped>
.consultation-container {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
}
.page-header { text-align: center; margin-bottom: 20px; }
.page-title { font-size: 28px; font-weight: 600; }
.consult-tabs { margin-top: 20px; }
.consult-form-card { padding: 30px; border-radius: 12px; }
.upload-tip { font-size: 12px; color: #999; margin-top: 10px; }
.submit-btn-wrapper { text-align: center; margin-top: 20px; }
.search-bar { background: #fff; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
.pagination { text-align: right; margin-top: 20px; }
.text-box { background: #f5f7fa; padding: 12px; border-radius: 6px; line-height: 1.8; }
.reply { background: #e6f7ff; }
</style>