<template>
  <div class="assessment-manager">
    <!-- 搜索与新增 -->
    <div class="search-bar">
      <el-input
          v-model="searchQuery"
          placeholder="请输入题目搜索"
          clearable
          style="width: 300px"
          @input="loadQuestions"
      />
      <el-button type="primary" icon="Plus" @click="openAddDialog">
        新增评测题目
      </el-button>
    </div>

    <!-- 题目列表 -->
    <el-table :data="filteredQuestions" stripe border style="margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">
            {{ getTypeName(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="question" label="题目内容" min-width="300" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button type="success" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button type="danger"@click="deleteQuestion(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table> <!-- 表格结束标签 -->

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="评测题目" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="题目类型" required>
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="综合健康" value="GENERAL" />
            <el-option label="高血压" value="HYPERTENSION" />
            <el-option label="糖尿病" value="DIABETES" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" required>
          <el-input v-model="form.question" type="textarea" rows="3" placeholder="请输入题目" />
        </el-form-item>
        <el-form-item label="选项配置" required>
          <el-input
              v-model="form.options"
              type="textarea"
              rows="4"
              placeholder='请输入JSON格式选项，例如：[{"content":"是","score":1},{"content":"否","score":0}]'
          />
        </el-form-item>
        <el-form-item label="排序号" required>
          <el-input-number v-model="form.sort" :min="1" placeholder="数字越小越靠前" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import request from '@/utils/request';

// 搜索
const searchQuery = ref('');
// 题目列表
const questions = ref([]);
// 弹窗
const dialogVisible = ref(false);
const form = reactive({
  id: null,
  type: '',
  question: '',
  options: '',
  sort: 1
});

// 加载题目
const loadQuestions = () => {
  request.get('/health/assessment/questions/all')
      .then(res => {
        console.log('后端接口完整返回:', res); // 调试日志
        // 关键修复：判断字符串类型的 "200"
        if (res.code === "200") {
          questions.value = res.data || [];
          console.log('赋值后题目列表:', questions.value); // 调试日志
        } else {
          ElMessage.error(res.msg || '加载题目失败');
        }
      })
      .catch(err => {
        console.error('接口请求失败:', err);
        ElMessage.error('加载题目失败，请检查后端服务');
      });
};

// 过滤题目（搜索）
const filteredQuestions = computed(() => {
  console.log('当前过滤后数据:', questions.value); // 调试日志
  if (!searchQuery.value) return questions.value;
  return questions.value.filter(q =>
      q.question?.includes(searchQuery.value) || q.type?.includes(searchQuery.value)
  );
});

// 打开新增弹窗
const openAddDialog = () => {
  Object.assign(form, { id: null, type: '', question: '', options: '', sort: 1 });
  dialogVisible.value = true;
};

// 打开编辑弹窗
const openEditDialog = (row) => {
  Object.assign(form, row);
  dialogVisible.value = true;
};

// 保存题目（修复 code 判断）
const saveQuestion = () => {
  if (!form.type || !form.question || !form.options) {
    ElMessage.warning('请完善必填项');
    return;
  }
  try {
    JSON.parse(form.options);
  } catch (e) {
    ElMessage.error('选项配置必须是合法JSON格式');
    return;
  }

  const api = form.id ? '/health/assessment/questions/update' : '/health/assessment/questions/add';
  request.post(api, form)
      .then(res => {
        if (res.code === "200") { // 修复 code 判断
          ElMessage.success('保存成功');
          dialogVisible.value = false;
          loadQuestions();
        } else {
          ElMessage.error(res.msg || '保存失败');
        }
      })
      .catch(err => {
        console.error(err);
        ElMessage.error('保存失败');
      });
};

// 删除题目（修复 code 判断）
const deleteQuestion = (id) => {
  ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete(`/health/assessment/questions/${id}`)
        .then(res => {
          if (res.code === "200") { // 修复 code 判断
            ElMessage.success('删除成功');
            loadQuestions();
          } else {
            ElMessage.error(res.msg || '删除失败');
          }
        })
        .catch(err => {
          console.error(err);
          ElMessage.error('删除失败');
        });
  }).catch(() => {});
};

// 类型转中文
const getTypeName = (type) => {
  const map = { GENERAL: '综合健康', HYPERTENSION: '高血压', DIABETES: '糖尿病' };
  return map[type] || type;
};

// 类型标签颜色
const getTypeTagType = (type) => {
  const map = { GENERAL: 'primary', HYPERTENSION: 'warning', DIABETES: 'danger' };
  return map[type] || 'info';
};

onMounted(() => {
  loadQuestions();
});
</script>

<style scoped>
.assessment-manager {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.search-bar {
  display: flex;
  gap: 15px;
  align-items: center;
}
</style>