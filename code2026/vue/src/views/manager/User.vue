<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input style="width: 300px" v-model="data.name" placeholder="请输入名称查询" :prefix-icon="Search"/>
      <el-button @click="load" type="success" style="margin-left:10px" >查询</el-button>
      <el-button @click="reset" type="info" style="margin-left: 10px">重置</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 10px">
        <el-button @click="handleAdd" type="primary">新增</el-button>
      </div>
      <div>
        <el-table :data="data.tableData" stripe style="width: 100%">
          <el-table-column prop="username" label="账号" />
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="avatar" label="头像">
            <template #default="scope">
              <!-- 有头像则显示用户头像,无头像则显示一个占位块 -->
              <!-- preview-src-list实现了头像点击预览,preview-teleported则令其浮于所有嵌套上不被遮挡 -->
              <el-image v-if="scope.row.avatar" style="width: 50px; height: 50px; display: block; border-radius: 50%"
                        :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" preview-teleported></el-image>
              <div v-else style="width: 50px; height: 50px; background: #eee; border-radius: 50%"></div>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" />
          <el-table-column label="操作" width="180px" fixed="right">
            <template #default="scope">
            <el-button type="success" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="del(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div class="card">
      <el-pagination v-model:current-page="data.pageNum" v-model:page-size="data.pageSize"
                     @current-change="load" background layout="total, prev, pager, next" :total="data.total" />
    </div>

    <el-dialog title="用户信息" v-model="data.formVisible" width="30%" destroy-on-close>
        <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px">
            <el-form-item prop="username" label="账号">
                <el-input :disabled="data.form.id !== undefined" v-model="data.form.username" placeholder="请输入账号" auto-complete="false"></el-input>
            </el-form-item>
          <el-form-item prop="name" label="姓名">
            <el-input v-model="data.form.name" placeholder="请输入姓名" auto-complete="false"></el-input>
          </el-form-item>
          <el-form-item prop="avatar" label="头像">
              <el-upload
                  :action="baseUrl + '/files/upload'"
                  list-type="picture"
                  :on-success="handleFileUpload"
              >
                <el-button type="primary">点击上传</el-button>
              </el-upload>
          </el-form-item>
        </el-form>

      <template #footer>
        <span class="el-dialog__footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </span>
      </template>


    </el-dialog>


  </div>
</template>

<script setup>
import { reactive , ref} from "vue";
import {Search} from "@element-plus/icons-vue";
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";

const baseUrl = import.meta.env.VITE_BASE_URL
const formRef = ref()
const data = reactive({
  name:null,
  tableData:[],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  formVisible: false,
  form: {},
  avatar: {},
  rules: {
    username: [
      {required: true, message: '请输入账号', trigger: 'blur'}
    ]
  }
})

//查询数据的函数
const load = () => {
  request.get('user/selectPage', {
    params:{
      pageNum: data.pageNum,
      pageSize: data.pageSize ,
      name: data.name
    }
  }).then(res => {
        if (res.code === '200') {
          data.tableData = res.data?.list
          data.total = res.data?.total
          // 新增打印：查看用户列表的avatar字段
          console.log("用户列表数据：", data.tableData);
        } else {
          ElMessage.error(res.msg)
        }
      }
  )
}
load()

//重置数据的函数
const reset = () => {
  data.name = null
  load()
}

//删除数据的函数
const del = (id) => {
  ElMessageBox.confirm('您确定删除吗？','系统提示',{ type: 'warning' }).then(res => {
    request.delete('/user/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

//新增按钮弹窗
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

//编辑用户信息按钮
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

//添加用户信息
const add = () => {
  request.post('/user/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('新建成功')
      data.formVisible = false
      load();
    } else {
      ElMessage.error(res.msg)
    }
  })
}

//修改用户信息
const  update = () => {
  request.put('/user/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('新建成功')
      data.formVisible = false
      load();
    } else {
      ElMessage.error(res.msg)
    }
  })
}

//新增用户的数据保存
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {    //前端表单校验
      data.form.id ? update() : add();    //id非空则修改，否则新建数据
    }
  })
}

//用户信息编辑表单图片上传Url获取
const handleFileUpload = (res) => {
  data.form.avatar = res.data
}
</script>
