<template>
  <div>
    <!-- 查询区域 (和药品管理样式完全一致) -->
    <div class="card" style="margin-bottom: 5px;">
      <el-select
          v-model="data.userId"
          style="width: 180px; margin-right: 10px"
          placeholder="选择用户"
          clearable
      >
        <el-option v-for="user in data.userList" :key="user.id" :label="user.name" :value="user.id" />
      </el-select>
      <el-select
          v-model="data.doctorId"
          style="width: 180px; margin-right: 10px"
          placeholder="选择医生"
          clearable
      >
        <el-option v-for="doc in data.doctorList" :key="doc.id" :label="doc.name" :value="doc.id" />
      </el-select>
      <el-select
          style="width: 150px; margin-right: 10px"
          v-model="data.status"
          placeholder="问诊状态"
          clearable
      >
        <el-option label="待回复" :value="0" />
        <el-option label="已回复" :value="1" />
        <el-option label="已关闭" :value="2" />
      </el-select>
      <el-button type="success" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="card" style="margin-bottom: 5px">
      <el-table
          :data="data.tableData"
          stripe
          border
          tooltip-effect="dark"
      >
        <el-table-column prop="id" label="问诊ID" width="80" />
        <el-table-column prop="userName" label="用户" min-width="100" />
        <el-table-column prop="doctorName" label="医生" min-width="100" />
        <el-table-column prop="content" label="问诊内容" show-overflow-tooltip min-width="200">
          <template #default="scope">
            <el-button type="text" @click="viewDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="warning" v-if="scope.row.status === 0">待回复</el-tag>
            <el-tag type="success" v-if="scope.row.status === 1">已回复</el-tag>
            <el-tag type="info" v-if="scope.row.status === 2">已关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发起时间" width="180" />
        <el-table-column prop="replyTime" label="回复时间" width="180" />
        <el-table-column label="操作" align="center" width="150">
          <template #default="scope">
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 (复用Goods的分页逻辑) -->
    <div class="card">
      <el-pagination
          background
          layout="total,prev,pager,next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @current-change="load"
      />
    </div>

    <!-- 问诊详情弹窗 (复用Goods的弹窗配置) -->
    <el-dialog
        v-model="data.detailVisible"
        title="问诊详情"
        width="60%"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户">{{ data.detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ data.detailData.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="问诊内容">{{ data.detailData.content }}</el-descriptions-item>
        <el-descriptions-item label="医生回复" v-if="data.detailData.replyContent">
          {{ data.detailData.replyContent }}
        </el-descriptions-item>
        <el-descriptions-item label="发起时间">{{ data.detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="回复时间" v-if="data.detailData.replyTime">
          {{ data.detailData.replyTime }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="success" v-if="data.detailData.status === 1">已回复</el-tag>
          <el-tag type="warning" v-if="data.detailData.status === 0">待回复</el-tag>
          <el-tag type="info" v-if="data.detailData.status === 2">已关闭</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.detailVisible = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref, onMounted, onBeforeUnmount } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";

// 复用Goods的响应式数据结构，统一管理所有状态
const data = reactive({
  // 分页参数
  pageNum: 1,
  pageSize: 10,
  total: 0,
  // 查询参数
  userId: null,
  doctorId: null,
  status: null,
  // 列表数据
  tableData: [],
  userList: [],
  doctorList: [],
  // 详情弹窗
  detailVisible: false,
  detailData: {}
});

// 页面加载：获取用户、医生、问诊数据 (复用Goods的初始化逻辑)
onMounted(() => {
  loadUserList();
  loadDoctorList();
  load();
});

// 组件销毁前清理（可选，保持和Goods一致的生命周期）
onBeforeUnmount(() => {
  data.tableData = [];
  data.userList = [];
  data.doctorList = [];
});

// 加载问诊列表 (对接后端接口，复用Goods的请求逻辑)
const load = () => {
  request.get('/consultation/admin/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.userId,
      doctorId: data.doctorId,
      status: data.status
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data.list;
      data.total = res.data.total;
      // 匹配用户名/医生名（优化：避免重复查找）
      data.tableData.forEach(item => {
        const user = data.userList.find(u => u.id === item.userId);
        const doctor = data.doctorList.find(d => d.id === item.doctorId);
        item.userName = user?.name || '未知';
        item.doctorName = doctor?.name || '未知';
      });
    } else {
      ElMessage.error('加载问诊列表失败：' + res.msg);
    }
  }).catch(err => {
    ElMessage.error('网络异常，加载失败');
    console.error(err);
  });
};
load()
// 加载用户列表 (复用Goods的分页查询逻辑)
const loadUserList = () => {
  request.get('/user/selectPage', { params: { pageNum: 1, pageSize: 100 } }).then(res => {
    if (res.code === '200') {
      data.userList = res.data.list;
    }
  }).catch(err => {
    console.error('加载用户列表失败：', err);
  });
};

// 加载医生列表 (过滤启用的医生，复用Goods的过滤逻辑)
const loadDoctorList = () => {
  request.get('/doctor/selectPage', { params: { pageNum: 1, pageSize: 100 } }).then(res => {
    if (res.code === '200') {
      data.doctorList = res.data.list.filter(d => d.status === 1);
    }
  }).catch(err => {
    console.error('加载医生列表失败：', err);
  });
};

// 重置查询 (复用Goods的重置逻辑，恢复初始状态)
const reset = () => {
  data.userId = null;
  data.doctorId = null;
  data.status = null;
  data.pageNum = 1;
  load();
  ElMessage.info('查询条件已重置');
};

// 查看详情 (复用Goods的弹窗赋值逻辑)
const viewDetail = (row) => {
  data.detailData = JSON.parse(JSON.stringify(row)); // 深拷贝避免原数据被修改
  data.detailVisible = true;
};

// 删除问诊记录 (复用Goods的删除确认+结果反馈逻辑)
const handleDelete = (id) => {
  ElMessageBox.confirm(
      '删除后数据无法恢复，您确定删除该问诊记录吗?',
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
  ).then(() => {
    request.delete(`/consultation/delete/${id}`).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功');
        load(); // 重新加载列表
      } else {
        ElMessage.error('删除失败：' + res.msg);
      }
    }).catch(err => {
      ElMessage.error('网络异常，删除失败');
      console.error(err);
    });
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};
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