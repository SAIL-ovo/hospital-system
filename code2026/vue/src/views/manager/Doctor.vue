<template>
  <div>
    <!-- 查询区域 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="请输入名称查询" :prefix-icon="Search"/>
      <el-button type="success" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <!-- 列表区域 -->
    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column prop="name" label="医生姓名" width="120"></el-table-column>
        <el-table-column prop="department" label="所属科室" width="120"></el-table-column>
        <el-table-column prop="title" label="医生职称" width="150" show-overflow-tooltip="true"></el-table-column>
        <el-table-column label="头像">
          <template #default="scope">
            <!-- 有头像则显示用户头像,无头像则显示一个占位块 -->
            <!-- preview-src-list实现了头像点击预览,preview-teleported则令其浮于所有嵌套上不被遮挡 -->
            <el-image v-if="scope.row.avatar" style="width: 50px; height: 50px; display: block; border-radius: 50%"
                      :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" preview-teleported></el-image>
            <div v-else style="width: 50px; height: 50px; background: #eee; border-radius: 50%"></div>
          </template>
        </el-table-column>
        <el-table-column prop="intro" label="医生简介" min-width="200"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.status === 1">在职</el-tag>
            <el-tag type="danger" v-else>离职</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="success" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页区域 -->
    <div class="card">
      <el-pagination
          @current-change="load"
          background
          layout="total, prev, pager, next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog title="医生信息" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="医生姓名" prop="name">
          <el-input v-model="data.form.name" placeholder="请输入医生姓名"></el-input>
        </el-form-item>
        <el-form-item label="登录密码" prop="password" :required="!data.form.id">
          <el-input v-model="data.form.password" type="password"   :placeholder="data.form.id ? '留空则不修改密码' : '请设置登录密码'" clearable ></el-input>
        </el-form-item>
        <el-form-item label="所属科室" prop="department">
          <el-select v-model="data.form.department" placeholder="请选择科室" style="width: 100%">
            <el-option label="内科" value="内科"></el-option>
            <el-option label="外科" value="外科"></el-option>
            <el-option label="儿科" value="儿科"></el-option>
            <el-option label="妇科" value="妇科"></el-option>
            <el-option label="皮肤科" value="皮肤科"></el-option>
            <el-option label="口腔科" value="口腔科"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="医生职称" prop="title">
          <el-input v-model="data.form.title" placeholder="如：主治医师/主任医师"></el-input>
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleImgSuccess">
            <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="医生简介" prop="intro">
          <el-input
              v-model="data.form.intro"
              type="textarea"
              :rows="4"
              placeholder="请输入医生简介（擅长领域、从业年限等）"
          ></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="data.form.status">
            <el-radio label="1">在职</el-radio>
            <el-radio label="0">离职</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import {Search} from "@element-plus/icons-vue";

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
const formRef = ref()
const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  doctorName: null, // 搜索用的医生姓名
  // 表单校验规则
  rules: {
    name: [
      { required: true, message: '请输入医生姓名', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请设置登录密码', trigger: 'blur' ,   // 只在新增时校验，编辑时不校验
        validator: (rule, value, callback) => {
          if (!data.form.id && !value) {
            callback(new Error('请设置登录密码'))
          } else {
            callback()
          }
        }},
    ],
    department: [
      { required: true, message: '请选择所属科室', trigger: 'change' },
    ],
    title: [
      { required: true, message: '请输入医生职称', trigger: 'blur' },
    ],
    intro: [
      { required: true, message: '请输入医生简介', trigger: 'blur' },
    ],
    status: [
      { required: true, message: '请选择状态', trigger: 'change' },
    ]
  }
})
const baseUrl = import.meta.env.VITE_BASE_URL

// 分页查询医生列表
const load = () => {
  request.get('/doctor/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.doctorName // 按姓名查询
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  }).catch(err => {
    ElMessage.error('网络异常，请稍后重试')
  })
}
// 初始化加载列表
load()

// 新增
const handleAdd = () => {
  // 重置表单，默认状态为在职
  data.form = { status: '1' }
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  // 深拷贝行数据，避免修改原数据；密码字段置空（编辑时可选修改）
  const formData = JSON.parse(JSON.stringify(row))
  formData.password = '' // 编辑时密码可选填，为空则不修改
  data.form = formData
  data.formVisible = true
}

// 新增
const add = () => {
  request.post('/doctor/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('新增成功')
      data.formVisible = false
      load() // 重新加载列表
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  }).catch(err => {
    ElMessage.error('网络异常，请稍后重试')
  })
}

// 编辑
const update = () => {
  request.put('/doctor/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('编辑成功')
      data.formVisible = false
      load() // 重新加载列表
    } else {
      ElMessage.error(res.msg || '编辑失败')
    }
  }).catch(err => {
    ElMessage.error('网络异常，请稍后重试')
  })
}

// 弹窗保存（新增/编辑通用）
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      // 有id是编辑，无id是新增
      data.form.id ? update() : add()
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm(
      '删除后数据无法恢复，您确定删除吗?',
      '删除确认',
      { type: 'warning' }
  ).then(() => {
    request.delete(`/doctor/delete/${id}`).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load() // 重新加载列表
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    }).catch(err => {
      ElMessage.error('网络异常，请稍后重试')
    })
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 重置查询条件
const reset = () => {
  data.doctorName = null
  data.pageNum = 1 // 重置页码
  load()
}

// 处理文件上传的钩子
const handleImgSuccess = (res) => {
  data.form.avatar = res.data  // res.data就是文件上传返回的文件路径，获取到路径后赋值表单的属性
}

load()
</script>


<style scoped>
.card {
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.dialog-footer {
  text-align: right;
}
</style>