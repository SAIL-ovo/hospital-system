<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.goodsName" style="width: 300px; margin-right: 10px" placeholder="请输入标题查询"></el-input>
      <el-button type="success" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-table :data="data.tableData" stripe>
        <el-table-column prop="title" label="标题" show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="desc" label="介绍" show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="img" label="图片">
          <template #default="scope">
            <el-image style="width: 200px; height: 100px; border-radius: 5px" :src="scope.row.img" :preview-src-list="[scope.row.img]" preview-teleported></el-image>
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

    <div class="card">
      <el-pagination
          @current-change="load"
          background
          layout="total, prev, pager, next"
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total">
      </el-pagination>
    </div>

    <el-dialog title="轮播图信息" v-model="data.formVisible" width="30%" :close-on-click-modal="false">
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px">
        <!-- 1. 标题输入框 -->
        <el-form-item label="标题" prop="title">
          <el-input v-model="data.form.title" placeholder="请输入轮播图标题"></el-input>
        </el-form-item>

        <!-- 2. 介绍输入框 -->
        <el-form-item label="介绍" prop="desc">
          <el-input
              v-model="data.form.desc"
              type="textarea"
              placeholder="请输入轮播图介绍（将显示在首页轮播图下方）"
              :rows="3">
          </el-input>
        </el-form-item>

        <!-- 3. 图片上传 -->
        <el-form-item label="图片" prop="img">
          <el-upload
              :action="baseUrl + '/files/upload'"
              :on-success="handleFileUpload"
              list-type="picture"
              :show-file-list="false">
            <img v-if="data.form.img" :src="data.form.img" style="width: 200px; height: 100px; border-radius: 5px;">
            <div v-else style="width: 200px; height: 150px; border: 1px dashed #ccc; display: flex; align-items: center; justify-content: center;">
              上传图片
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  goodsName: null, // 改为按标题查询
  form: {},
  rules: {
    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
    desc: [{ required: true, message: '请输入介绍', trigger: 'blur' }],
    img: [{ required: true, message: '请上传图片', trigger: 'blur' }]
  }
})


onMounted(() => {
  load()
})

const load = () => {
  request.get('/carousel/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.goodsName // 改为按标题查询
    }
  }).then(res => {
    data.tableData = res.data.list
    data.total = res.data.total
  })
}

const handleFileUpload = (res) => {
  data.form.img = res.data
}

const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }).then(() => {
    request.delete('/carousel/delete/' + id).then(res => {
      ElMessage.success('删除成功')
      load()
    })
  })
}

const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const url = data.form.id ? '/carousel/update' : '/carousel/add'
      const method = data.form.id ? 'put' : 'post'
      request[method](url, data.form).then(res => {
        ElMessage.success('操作成功')
        data.formVisible = false
        load()
      })
    }
  })
}

const reset = () => {
  data.goodsName = null
  load()
}
</script>