<template>
  <div class="assessment-page">
    <el-page-header @back="goBack" content="健康自评">
      <el-button type="primary" @click="goToRecord">查看我的自评记录</el-button>
    </el-page-header>

    <div class="assessment-wrapper">
      <!-- 自评类型选择 -->
      <el-card class="type-select-card" shadow="hover">
        <el-form label-width="80px" :model="assessmentForm" inline>
          <el-form-item label="自评类型：" label-width="100px">
            <el-select v-model="assessmentForm.type" placeholder="请选择自评类型" @change="loadAssessmentQuestions">
              <el-option label="综合健康" value="GENERAL"></el-option>
              <el-option label="高血压" value="HYPERTENSION"></el-option>
              <el-option label="糖尿病" value="DIABETES"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 题目列表 -->
      <el-card v-if="assessmentQuestions.length > 0" class="question-card" shadow="hover">
        <div class="question-title">请完成以下题目（每题选择对应选项）</div>
        <div class="question-item" v-for="(question, index) in assessmentQuestions" :key="question.id">
          <div class="question-content">{{ index + 1 }}. {{ question.question }}</div>
          <el-radio-group v-model="answerMap[question.id]" @change="calculateTotalScore">
            <el-radio
                v-for="option in parseOptions(question.options)"
                :key="option.score"
                :label="option.score"
            >
              {{ option.content }}（{{ option.score }}分）
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 总分展示 & 提交按钮 -->
        <div class="submit-area">
          <el-tag type="info" size="large">本次自评总分：{{ totalScore }}</el-tag>
          <el-button type="primary" size="large" @click="submitAssessment" :disabled="totalScore === 0">
            提交自评结果
          </el-button>
        </div>
      </el-card>

      <!-- 无数据提示 -->
      <el-empty v-if="assessmentForm.type && assessmentQuestions.length === 0" description="暂无该类型的自评题目"></el-empty>

      <!-- 结果弹窗 -->
      <el-dialog v-model="resultDialogVisible" title="自评结果" width="50%">
        <div v-if="assessmentResult">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="自评类型">{{ getTypeName(assessmentForm.type) }}</el-descriptions-item>
            <el-descriptions-item label="总分">{{ totalScore }}</el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag :type="getRiskTagType(assessmentResult.riskLevel)">{{ getRiskName(assessmentResult.riskLevel) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="健康建议">{{ assessmentResult.suggestion }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <template #footer>
          <el-button @click="resultDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="resetAssessment">重新自评</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import request from '@/utils/request';

const router = useRouter();

// 自评表单
const assessmentForm = reactive({
  type: ''
});
// 自评题目列表
const assessmentQuestions = reactive([]);
// 答案映射
const answerMap = reactive({});
// 总分
const totalScore = ref(0);
// 自评结果
const assessmentResult = ref(null);
// 结果弹窗显隐
const resultDialogVisible = ref(false);

// 解析选项JSON（防止解析失败）
const parseOptions = (optionsStr) => {
  try {
    return JSON.parse(optionsStr);
  } catch (e) {
    console.error('选项解析失败:', e);
    return [];
  }
};

const loadAssessmentQuestions = () => {
  if (!assessmentForm.type) return;
  // 清空旧数据（确保响应式更新）
  assessmentQuestions.splice(0, assessmentQuestions.length);
  Object.keys(answerMap).forEach(key => delete answerMap[key]);
  totalScore.value = 0;

  request.get(`/health/assessment/questions/${assessmentForm.type}`)
      .then(res => {
        console.log('题目接口返回:', res);
        let data = [];
        // 关键修复：判断字符串类型的 "200"
        if (res.code === "200" && res.data) {
          data = res.data;
        } else if (Array.isArray(res)) {
          data = res;
        }

        if (data.length > 0) {
          assessmentQuestions.push(...data);
        } else {
          ElMessage.warning('暂无该类型的自评题目');
        }
      })
      .catch(err => {
        console.error('加载题目失败:', err);
        ElMessage.error('加载题目失败，请稍后重试');
      });
};
// 计算总分
const calculateTotalScore = () => {
  let sum = 0;
  Object.values(answerMap).forEach(score => {
    sum += Number(score) || 0;
  });
  totalScore.value = sum;
};

// 提交自评结果
// 提交自评结果
const submitAssessment = () => {
  const userStr = localStorage.getItem('system-user');
  if (!userStr) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  const user = JSON.parse(userStr);
  if (!user.id) {
    ElMessage.warning('用户信息异常');
    return;
  }

  const submitData = {
    userId: Number(user.id), // 强制转为数字（适配后端Long类型）
    type: assessmentForm.type, // 与后端实体type字段一致，无需修改
    totalScore: totalScore.value
  };

  request.post('/health/assessment/record', submitData)
      .then(res => {
        console.log('提交结果返回:', res);
        if (res.code === '200' && res.data) {
          assessmentResult.value = res.data; // 接收后端返回的风险等级/建议
          resultDialogVisible.value = true;
        } else {
          ElMessage.error(res.msg || '提交失败');
        }
      })
      .catch(err => {
        console.error('提交失败:', err);
        ElMessage.error('提交失败，请检查后端接口');
      });
};

// 重置自评
const resetAssessment = () => {
  assessmentForm.type = '';
  assessmentQuestions.length = 0;
  Object.keys(answerMap).forEach(key => delete answerMap[key]);
  totalScore.value = 0;
  assessmentResult.value = null;
  resultDialogVisible.value = false;
};

// 类型转中文
const getTypeName = (type) => {
  const map = { GENERAL: '综合健康', HYPERTENSION: '高血压', DIABETES: '糖尿病' };
  return map[type] || '未知类型';
};

// 风险等级转中文
const getRiskName = (level) => {
  const map = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险' };
  return map[level] || '未知';
};

// 风险等级转标签类型
const getRiskTagType = (level) => {
  const map = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' };
  return map[level] || 'info';
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 跳转到自评记录页
const goToRecord = () => {
  router.push('/front/health-assessment-record');
};
</script>

<style scoped>
.assessment-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.assessment-wrapper {
  margin-top: 20px;
}
.type-select-card {
  margin-bottom: 20px;
}
.question-card {
  padding: 24px;
}
.question-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}
.question-item {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.question-content {
  font-size: 16px;
  margin-bottom: 12px;
}
.submit-area {
  margin-top: 30px;
  text-align: center;
}
.submit-area .el-tag {
  margin-right: 20px;
}
</style>