<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.patientName" style="width: 300px; margin-right: 10px" placeholder="请输入患者姓名查询"></el-input>
      <el-button type="success" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column prop="userId" label="患者ID"></el-table-column>
        <el-table-column prop="patientName" label="患者姓名"></el-table-column>
        <!-- 直接渲染后端返回的 doctorName -->
        <el-table-column prop="doctorName" label="医生姓名" />
        <el-table-column prop="date" label="日期"></el-table-column>
        <el-table-column prop="time" label="时段"></el-table-column>
        <el-table-column prop="status" label="就诊状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '待确认'" type="warning">待确认</el-tag>
            <el-tag v-if="scope.row.status === '已确认'" type="success">已确认</el-tag>
            <el-tag v-if="scope.row.status === '已取消'" type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
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
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
      />
    </div>

    <el-dialog title="预约信息" width="30%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="患者" prop="userId">
          <el-select style="width: 100%" v-model="data.form.userId">
            <el-option v-for="item in data.userList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="医生" prop="doctorId">
          <el-select style="width: 100%" v-model="data.form.doctorId">
            <el-option v-for="item in data.doctorList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker type="date" value-format="YYYY-MM-DD" v-model="data.form.date" placeholder="选择日期"></el-date-picker>
        </el-form-item>
        <el-form-item label="时段" prop="time">
          <el-select v-model="data.form.time">
            <el-option label="08:00-10:00" value="08:00-10:00"></el-option>
            <el-option label="10:00-12:00" value="10:00-12:00"></el-option>
            <el-option label="14:00-16:00" value="14:00-16:00"></el-option>
            <el-option label="16:00-18:00" value="16:00-18:00"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="data.form.status">
            <el-radio-button label="待确认" value="待确认"></el-radio-button>
            <el-radio-button label="已确认" value="已确认"></el-radio-button>
            <el-radio-button label="已取消" value="已取消"></el-radio-button>
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
import { reactive, ref, onMounted } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";

const formRef = ref()
const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  // 编辑/新增需要的列表（临时用硬编码，解决404问题）
  userList: [
    { id: 1, name: "小张1" },
    { id: 2, name: "派大星" },
    { id: 13, name: "珊迪" }
  ],
  doctorList: [
    { id: 1, name: "张建军" },
    { id: 2, name: "李娜" },
    { id: 3, name: "王强" },
    { id: 4, name: "陈曦" },
    { id: 5, name: "刘德远" },
    { id: 6, name: "赵文静" }
  ],
  patientName: null,
  rules: {
    userId: [{ required: true, message: '请选择患者', trigger: 'change' }],
    doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
    date: [{ required: true, message: '请选择日期', trigger: 'change' }],
    time: [{ required: true, message: '请选择时段', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  }
})

// 分页查询（核心：只请求预约接口，依赖后端返回 doctorName）
const load = () => {
  request.get('/appointment/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      patientName: data.patientName
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || [];
      data.total = res.data?.total || 0;
      // 打印验证：确认后端是否返回了 doctorName
      console.log('预约数据（含医生姓名）:', data.tableData);
    }
  }).catch(err => {
    ElMessage.error('预约数据加载失败');
    console.error(err);
  })
}

// 页面加载时只执行预约查询
onMounted(() => {
  load();
})


// 编辑
const handleEdit = (row) => {
  data.form = { ...row };
  data.formVisible = true;
}

// 保存（新增/编辑）
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const requestFn = data.form.id
          ? request.put('/appointment/update', data.form)
          : request.post('/appointment/add', data.form);

      requestFn.then(res => {
        if (res.code === '200') {
          ElMessage.success('操作成功');
          data.formVisible = false;
          load(); // 重新加载数据
        } else {
          ElMessage.error(res.msg || '操作失败');
        }
      }).catch(err => {
        ElMessage.error('请求失败');
        console.error(err);
      })
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' })
      .then(() => {
        request.delete(`/appointment/delete/${id}`)
            .then(res => {
              if (res.code === '200') {
                ElMessage.success('删除成功');
                load();
              } else {
                ElMessage.error(res.msg || '删除失败');
              }
            })
            .catch(err => {
              ElMessage.error('删除请求失败');
              console.error(err);
            })
      })
      .catch(() => {})
}

// 重置
const reset = () => {
  data.patientName = null;
  data.pageNum = 1;
  load();
}
</script>