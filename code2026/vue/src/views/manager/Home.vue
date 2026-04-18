<template>
  <div class="manager-home">
    <!-- 1. 数据概览卡片 -->
    <div class="data-cards">
      <div class="card-item" v-for="item in dataCards" :key="item.key">
        <div class="card-icon" :style="{ backgroundColor: item.color }">
          <el-icon :size="24" :color="'#fff'">
            <component :is="item.icon"></component>
          </el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">{{ item.value }}</div>
          <div class="card-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 2. 快捷操作区 -->
    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <div class="action-buttons">
        <el-button type="primary" icon="Plus" @click="router.push('/manager/goods')">
          药品管理
        </el-button>
        <el-button type="success" icon="Picture" @click="router.push('/manager/carousel')">
          轮播图管理
        </el-button>
        <el-button type="warning" icon="Calendar" @click="router.push('/manager/appointment')">
          预约管理
        </el-button>
        <el-button type="info" icon="User" @click="router.push('/manager/user')">
          用户管理
        </el-button>
        <el-button style="background-color: #66B1FF; color: white" icon="Menu" @click="router.push('/manager/category')">
          分类管理
        </el-button>
        <el-button style="background-color: #997bff; color: white" icon="Menu" @click="router.push('/manager/assessment')">
          评测管理
        </el-button>
        <el-button style="background-color: #f61ceb; color: white" icon="Message" @click="router.push('/manager/consultation')">
          问诊记录
        </el-button>
      </div>
    </div>

    <!-- 3. 库存预警（对接真实数据库+动态分类） -->
    <div class="update-card">
      <h3 class="section-title">库存预警</h3>
      <el-table :data="lowStockGoods" stripe border :empty-text="emptyText">
        <el-table-column prop="name" label="药品名称" width="150"></el-table-column>
        <el-table-column prop="store" label="当前库存" width="100">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.store < 10">{{ scope.row.store }}</el-tag>
            <span v-else>{{ scope.row.store }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="药品分类" width="120"></el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="text" @click="editGoods(scope.row.id)">补货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  User,
  ShoppingCart,
  Picture,
  Calendar,
  Warning,
  Message
} from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();
const emptyText = '暂无相关数据';

// 1. 数据概览卡片数据
const dataCards = reactive([
  { key: 'appointment', label: '今日预约数', value: 0, icon: Calendar, color: '#409eff' },
  { key: 'stock', label: '库存预警数', value: 0, icon: Warning, color: '#f56c6c' },
  { key: 'carousel', label: '轮播图总数', value: 0, icon: Picture, color: '#67c23a' },
  { key: 'user', label: '注册用户数', value: 0, icon: User, color: '#909399' },
  { key: 'goods', label: '药品总数', value: 0, icon: ShoppingCart, color: '#e6a23c' }
]);

// 2. 核心数据
const lowStockGoods = reactive([]);     // 库存预警药品
const categoryMap = reactive({});      // 动态分类映射 {id: name}

// 页面挂载时加载数据（先加载分类，再加载药品）
onMounted(() => {
  loadAllCategories(); // 先加载分类
  loadDataOverview();  // 加载其他概览数据
});

// 加载所有分类（对接你的 /category/selectAll 接口）
const loadAllCategories = () => {
  request.get('/category/selectAll')
      .then(res => {
        if (res.code === '200') {
          const categories = res.data || [];
          // 构建分类映射表：{分类ID: 分类名称}
          categories.forEach(cat => {
            categoryMap[cat.id] = cat.name;
          });
          // 分类加载完成后，再加载库存预警药品
          loadLowStockGoodsFromDB();
        }
      })
      .catch(err => {
        console.error('获取药品分类失败：', err);
        ElMessage.error('获取药品分类失败');
        // 即使分类加载失败，也尝试加载药品
        loadLowStockGoodsFromDB();
      });
};

// 加载真实数据概览（对接数据库）
const loadDataOverview = () => {
  // 今日预约数
  request.get('/appointment/countToday').then(res => {
    if (res.code === '200') dataCards.find(i => i.key === 'appointment').value = res.data || 0;
  }).catch(err => console.error('获取今日预约数失败：', err));

  // 轮播图总数
  request.get('/carousel/countAll').then(res => {
    if (res.code === '200') dataCards.find(i => i.key === 'carousel').value = res.data || 0;
  }).catch(err => console.error('获取轮播图总数失败：', err));

  // 注册用户数
  request.get('/user/countAll').then(res => {
    if (res.code === '200') dataCards.find(i => i.key === 'user').value = res.data || 0;
  }).catch(err => console.error('获取注册用户数失败：', err));

  // 药品总数
  request.get('/goods/countAll').then(res => {
    if (res.code === '200') dataCards.find(i => i.key === 'goods').value = res.data || 0;
  }).catch(err => console.error('获取药品总数失败：', err));

  // 库存预警数（从全量药品中统计）
  request.get('/goods/selectAll').then(res => {
    if (res.code === '200') {
      const allGoods = res.data || [];
      const lowStockCount = allGoods.filter(g => g.store < 10).length;
      dataCards.find(i => i.key === 'stock').value = lowStockCount;
    }
  }).catch(err => console.error('获取库存预警数失败：', err));
};

// 从数据库加载库存预警药品（store < 10）
const loadLowStockGoodsFromDB = () => {
  request.get('/goods/selectAll')
      .then(res => {
        if (res.code === '200') {
          const allGoods = res.data || [];
          // 过滤出库存 < 10 的药品，并动态匹配分类名称
          const lowStock = allGoods.filter(g => g.store < 10).map(g => ({
            ...g,
            // 从动态分类映射中获取名称，不存在则显示"未知分类"
            categoryName: categoryMap[g.categoryId] || '未知分类'
          }));
          lowStockGoods.length = 0;
          lowStockGoods.push(...lowStock);
        }
      })
      .catch(err => {
        console.error('获取库存预警药品失败：', err);
        ElMessage.error('获取库存预警药品失败');
      });
};

// 编辑药品（跳转到药品编辑页）
const editGoods = (id) => {
  router.push({ path: '/manager/goods', query: { editId: id } });
  ElMessage.info('跳转到药品补货页面');
};
</script>

<style scoped>
.manager-home {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 数据概览卡片样式 */
.data-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.card-item {
  flex: 1;
  min-width: 180px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 15px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
}

.card-label {
  font-size: 14px;
  color: #666;
}

/* 快捷操作区样式 */
.quick-actions {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.section-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 库存预警卡片样式 */
.update-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 适配不同屏幕尺寸 */
@media (max-width: 768px) {
  .data-cards {
    flex-direction: column;
  }
}
</style>