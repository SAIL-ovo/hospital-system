<template>
  <div class="consultation-page">
    <div class="page-header">
      <h2>问诊信息管理</h2>
    </div>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" type="card">
      <el-tab-pane label="待回复问诊" name="0"></el-tab-pane>
      <el-tab-pane label="已回复问诊" name="1"></el-tab-pane>
    </el-tabs>
    <el-table
        :data="tableData"
        border
        stripe
        empty-text="暂无问诊数据"
        align="center"
    >
      <el-table-column label="患者姓名" width="120">
        <template #default="scope">
          {{ matchPatientName(scope.row.userId || scope.row.user_id) }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发起时间" width="200" />
      <el-table-column prop="content" label="问诊内容" min-width="250" />
      <el-table-column prop="replyContent" label="医生回复" min-width="250" v-if="activeTab === '1'" />
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openReply(scope.row)">回复</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        @size-change="getData"
        @current-change="getData"
        style="margin-top:20px;text-align:right"
    />
    <el-dialog title="回复问诊" v-model="replyVisible" width="600px">
      <el-form label-width="80px" :model="replyForm">
        <el-form-item label="患者提问">
          <div class="q-box">{{ currentConsult.content }}</div>
        </el-form-item>
        <el-form-item label="我的回复">
          <el-input
              v-model="replyForm.content"
              type="textarea"
              rows="5"
              placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('0')
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const userList = ref([]) // 存储完整用户列表

// 回复弹窗
const replyVisible = ref(false)
const currentConsult = ref({})
const replyForm = reactive({ content: '' })

// ========== 初始化加载 ==========
onMounted(async () => {
  await loadUserList()
  await getData()
})

// ========== 加载问诊列表（核心：打印完整问诊项，确认字段名） ==========
const getData = async () => {
  const doctor = JSON.parse(localStorage.getItem('system-user') || '{}')
  if (!doctor.id) {
    ElMessage.warning('未获取到医生身份，请重新登录')
    return
  }

  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      doctorId: Number(doctor.id),
      status: Number(activeTab.value)
    }

    const res = await request.get('/consultation/doctor/selectPage', { params })
    if (res.code === 200 || res.code === '200') {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
      // 🔥 打印完整问诊项，确认用户ID字段名到底是userId还是user_id
      tableData.value.forEach(item => {
        console.log('完整问诊项：', item)
        console.log(`问诊ID: ${item.id} → userId: ${item.userId} | user_id: ${item.user_id}`)
      })
    } else {
      ElMessage.error(res.msg || '加载问诊数据失败')
    }
  } catch (err) {
    ElMessage.error('接口请求异常')
    console.error('问诊列表接口错误：', err)
  }
}

// ========== 加载所有用户 ==========
const loadUserList = async () => {
  try {
    const res = await request.get('/user/selectPage', { params: { pageNum: 1, pageSize: 1000 } })
    // 兼容后端返回结构
    const users = Array.isArray(res.data) ? res.data : (res.data?.list || [])
    userList.value = users
    console.log('已加载用户列表：', userList.value)
  } catch (err) {
    console.error('加载用户列表失败：', err)
  }
}

// ========== 匹配患者姓名 ==========
const matchPatientName = (userId) => {
  console.log(`匹配姓名：传入用户ID=${userId}（类型：${typeof userId}）`)
  if (!userId) return '未知患者'

  // 强制转数字匹配，彻底避免类型问题
  const patient = userList.value.find(user => Number(user.id) === Number(userId))
  const name = patient?.name || patient?.username || '未知患者'
  console.log(`匹配结果：${userId} → ${name}`)
  return name
}

// ========== 切换标签页 ==========
const handleTabChange = async () => {
  pageNum.value = 1
  await loadUserList()
  await getData()
}

// ========== 打开回复弹窗 ==========
const openReply = (row) => {
  if (!row.id) {
    ElMessage.warning('问诊ID缺失，无法回复')
    return
  }
  currentConsult.value = row
  replyForm.content = ''
  replyVisible.value = true
}

// ========== 提交回复 ==========
const submitReply = async () => {
  if (!replyForm.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  const doctor = JSON.parse(localStorage.getItem('system-user') || '{}')
  if (!doctor.id) {
    ElMessage.warning('医生身份验证失败，请重新登录')
    return
  }

  const params = {
    id: currentConsult.value.id,
    replyContent: replyForm.content
  }

  try {
    const res = await request.put('/consultation/reply', null, { params })
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('回复成功！')
      replyVisible.value = false
      await getData()
    } else {
      ElMessage.error(res.msg || '回复失败')
    }
  } catch (err) {
    ElMessage.error('回复请求失败')
    console.error('回复接口错误：', err)
  }
}
</script>

<style scoped>
.consultation-page { padding: 20px; background: #f5f7fa; min-height: 100vh; }
.page-header { margin-bottom: 20px; }
.q-box { padding: 10px; background: #f8f8f8; border-radius: 4px; }
</style>