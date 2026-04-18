<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-grid">
        <div class="hero-copy">
          <p class="hero-kicker">Community Care Portal</p>
          <h1>一站式整合社区医疗服务，全方位守护居民健康生活</h1>
          <p class="hero-desc">
            这里是面向社区居民的线上服务首页，围绕预约挂号、医生团队、健康资讯、健康档案与日常咨询等高频场景组织内容，让看病、问诊和健康管理都更方便。
          </p>
          <div class="hero-mini-note">
            <span class="note-dot"></span>
            用数字化，让社区医院服务更清晰、更贴心、更高效
          </div>
        </div>

        <div class="hero-visual">
          <div class="hero-panel hero-carousel-panel">
            <div class="panel-badge">社区服务焦点</div>
            <el-carousel height="560px" :interval="5000" indicator-position="outside" arrow="hover" class="hero-carousel">
              <el-carousel-item v-for="(item, index) in carouselList" :key="index">
                <div class="hero-slide">
                  <img :src="item.img" :alt="item.title" class="hero-slide-image" />
                  <div class="hero-slide-overlay"></div>
                  <div class="hero-slide-content">
                    <p>社区医疗服务</p>
                    <h3>{{ item.title }}</h3>
                    <span>{{ item.desc }}</span>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
        </div>
      </div>
    </section>

    <section class="quick-entry-section">
      <div class="section-head compact">
        <div>
          <p class="section-kicker">Quick Access</p>
          <h2>常用入口</h2>
        </div>
        <span class="section-hint">从这里快速跳转到高频功能</span>
      </div>

      <div class="quick-entry-grid">
        <div class="entry-card" v-for="item in quickEntryList" :key="item.id" @click="handleEntryClick(item)">
          <div class="entry-icon">
            <el-icon :size="28"><component :is="item.icon" /></el-icon>
          </div>
          <div class="entry-meta">
            <strong>{{ item.name }}</strong>
            <span>点击进入对应服务</span>
          </div>
        </div>
      </div>
    </section>

    <section class="about-section">
      <div class="section-head">
        <div>
          <p class="section-kicker">About Us</p>
          <h2>关于我们</h2>
        </div>
      </div>

      <div class="about-layout">
        <div class="about-copy-card">
          <p class="about-label">社区卫生服务中心</p>
          <h3>面向居民日常诊疗与连续健康管理的基层医疗服务平台。</h3>
          <p>我们聚焦常见病诊疗、慢病随访、居民健康管理、家庭医生服务与基础公共卫生工作，希望通过更清晰的线上入口，让居民更方便地获得社区医疗支持。</p>
          <p>首页内容围绕“找服务、找医生、看资讯、做预约”四条主线组织，减少查找成本，让第一次进入系统的居民也能快速上手。</p>
        </div>

        <div class="about-gallery">
          <el-carousel class="about-carousel" :interval="5000" indicator-position="outside" arrow="hover" height="460px">
            <el-carousel-item v-for="(img, index) in aboutImgs" :key="index">
              <div class="about-slide" :style="{ backgroundImage: `url(${img})` }"></div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>
    </section>

    <section id="department" class="section-block">
      <div class="section-head">
        <div>
          <p class="section-kicker">Departments</p>
          <h2>科室设置</h2>
        </div>
        <span class="section-hint">覆盖社区高频就诊与基础诊疗场景</span>
      </div>

      <div class="department-grid">
        <article class="department-card" v-for="item in departmentList" :key="item.id">
          <div class="department-icon">{{ item.icon }}</div>
          <div class="department-text">
            <h4>{{ item.name }}</h4>
            <p>{{ item.desc }}</p>
          </div>
        </article>
      </div>
    </section>

    <section id="doctor" class="section-block">
      <div class="section-head">
        <div>
          <p class="section-kicker">Doctors</p>
          <h2>医生团队</h2>
        </div>
        <span class="section-hint">点击卡片可直接带入预约医生</span>
      </div>

      <div class="doctor-grid">
        <div v-if="doctorList.length === 0" class="empty-card">暂无在职医生信息</div>

        <article class="doctor-card" v-for="doc in doctorList" :key="doc.id" @click="chooseDoctor(doc.id)">
          <div class="doctor-top">
            <el-avatar
              :size="84"
              :src="doc.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
              fit="cover"
              class="doctor-avatar"
            >
              {{ doc.name?.substring(0, 1) || '医' }}
            </el-avatar>
            <div class="doctor-basic">
              <h3>{{ doc.name || '未知医生' }}</h3>
              <p>{{ doc.department || '未分配科室' }} · {{ doc.title || '医师' }}</p>
            </div>
          </div>

          <p class="doctor-desc">{{ doc.desc || '暂无医生简介' }}</p>

          <div class="doctor-action">
            <span>选择该医生预约</span>
            <el-icon><Check /></el-icon>
          </div>
        </article>
      </div>
    </section>

    <section id="news" class="section-block">
      <div class="section-head">
        <div>
          <p class="section-kicker">Health News</p>
          <h2>健康资讯</h2>
        </div>
        <span class="section-hint">社区居民常见健康提醒与服务通知</span>
      </div>

      <div class="news-grid">
        <article class="news-card" v-for="item in newsList" :key="item.id">
          <div class="news-cover">
            <img :src="item.img" alt="" class="news-img" />
          </div>
          <div class="news-body">
            <span class="news-date">{{ item.time }}</span>
            <h4>{{ item.title }}</h4>
            <p>{{ item.desc }}</p>
          </div>
        </article>
      </div>
    </section>

    <section id="appointment" class="appointment-section">
      <div class="section-head">
        <div>
          <p class="section-kicker">Appointment</p>
          <h2>预约挂号</h2>
        </div>
        <span class="section-hint">选择科室、日期、时段和医生后即可提交</span>
      </div>

      <div class="appointment-layout">
        <div class="appointment-side">
          <div class="appointment-tip-card">
            <h3>预约前小提示</h3>
            <ul>
              <li>请优先确认就诊科室与医生是否匹配。</li>
              <li>当天已过时段无法再选择，系统会自动禁用。</li>
              <li>提交成功后，可在右上角“我的预约”中查看记录。</li>
            </ul>
          </div>
        </div>

        <div class="appointment-form-wrap">
          <el-form :model="appointmentForm" label-width="92px" class="appointment-form" :rules="appointmentRules" ref="appointmentFormRef">
            <el-row :gutter="18">
              <el-col :xs="24" :sm="12">
                <el-form-item label="就诊科室" prop="departmentId">
                  <el-select v-model="appointmentForm.departmentId" placeholder="请选择科室" class="field-control">
                    <el-option v-for="dept in departmentList" :key="dept.id" :label="dept.name" :value="dept.id" />
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="12">
                <el-form-item label="就诊日期" prop="date">
                  <el-date-picker v-model="appointmentForm.date" type="date" placeholder="选择日期" class="field-control" :disabled-date="disabledPastDate" />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="12">
                <el-form-item label="就诊时段" prop="time">
                  <el-select v-model="appointmentForm.time" placeholder="请选择时段" class="field-control" @change="() => validateTimeIsValid()">
                    <el-option
                      v-for="item in timeOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                      :disabled="isTimePassed(item)"
                      @click="handleDisabledTimeClick(item)"
                    />
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="12">
                <el-form-item label="就诊医生" prop="doctorId">
                  <el-select v-model="appointmentForm.doctorId" placeholder="请选择医生" class="field-control">
                    <el-option v-for="doc in doctorList" :key="doc.id" :label="doc.name" :value="doc.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item class="appointment-btn-wrapper">
              <el-button type="primary" size="large" class="submit-btn" @click="submitAppointment">
                <el-icon><Check /></el-icon>
                提交预约
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </section>

    <section id="contact" class="contact-section">
      <div class="section-head light inner-head">
        <div>
          <p class="section-kicker">Contact</p>
          <h2>联系我们</h2>
        </div>
      </div>

      <div class="contact-grid">
        <div class="contact-card">
          <span>咨询电话</span>
          <strong>028-62600160</strong>
          <p>工作日服务时段可拨打，紧急情况请及时前往线下就诊。</p>
        </div>
        <div class="contact-card">
          <span>值班联系</span>
          <strong>13880624228</strong>
          <p>用于社区服务沟通与基础健康咨询。</p>
        </div>
        <div class="contact-card">
          <span>联系邮箱</span>
          <strong>snqshy@126.com</strong>
          <p>适用于意见反馈、服务建议与业务联系。</p>
        </div>
      </div>

      <p class="copyright-text">版权所有：社区卫生服务中心 ｜ 页面已按居民端首页场景优化展示</p>
    </section>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Document, User, Calendar, Location, Phone, Check, Comment } from '@element-plus/icons-vue';
import request from '@/utils/request';
import router from '@/router';

import h1 from '@/assets/imgs/h1.png';
import h2 from '@/assets/imgs/h2.png';
import h3 from '@/assets/imgs/h3.jpg';
import ZX1 from '@/assets/imgs/ZX1.png';
import ZX2 from '@/assets/imgs/ZX2.png';
import ZX3 from '@/assets/imgs/ZX3.png';
import ZX4 from '@/assets/imgs/ZX4.png';
import ZX5 from '@/assets/imgs/ZX5.png';

const quickEntryList = reactive([
  { id: 1, name: '预约挂号', icon: Calendar, path: '/front/home#appointment' },
  { id: 2, name: '科室导航', icon: Location, path: '/front/home#department' },
  { id: 3, name: '医生团队', icon: User, path: '/front/home#doctor' },
  { id: 4, name: '健康资讯', icon: Document, path: '/front/home#news' },
  { id: 5, name: '联系我们', icon: Phone, path: '/front/home#contact' },
  { id: 6, name: '健康自评', icon: Check, path: '/front/health-assessment' },
  { id: 7, name: '在线问诊', icon: Comment, path: '/front/consultation' }
]);

const carouselList = reactive([]);
const departmentList = reactive([]);
const doctorList = reactive([]);
const newsList = reactive([]);

const appointmentFormRef = ref(null);
const appointmentForm = reactive({
  departmentId: '',
  date: '',
  time: '',
  doctorId: ''
});

const appointmentRules = reactive({
  departmentId: [{ required: true, message: '请选择就诊科室', trigger: 'change' }],
  date: [{ required: true, message: '请选择就诊日期', trigger: 'change' }],
  time: [{ required: true, message: '请选择就诊时段', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择就诊医生', trigger: 'change' }]
});

onMounted(() => {
  loadCarousel();
  loadDepartments();
  loadDoctors();
  loadNews();
});

const loadCarousel = () => {
  request.get('/carousel/selectAll').then(res => {
    const data = res.code === '200' ? res.data : res;
    carouselList.length = 0;
    data.forEach(item => {
      carouselList.push({
        title: item.title || '专业社区医疗服务，守护居民日常健康',
        desc: item.desc || '围绕问诊、慢病管理、健康服务与基础医疗场景提供支持',
        img: item.img || 'https://source.unsplash.com/random/1920x1080?hospital'
      });
    });
  }).catch(err => {
    console.error('轮播图加载失败：', err);
    ElMessage.warning('轮播图数据加载失败');
  });
};

const aboutImgs = reactive([h1, h2, h3]);

const loadDepartments = () => {
  departmentList.length = 0;
  departmentList.push(
    { id: 1, name: '内科', icon: '🩺', desc: '处理常见内科疾病、慢病复诊与基础诊疗服务。' },
    { id: 2, name: '外科', icon: '🩹', desc: '面向常规外科处理、伤口评估与基础处置。' },
    { id: 3, name: '儿科', icon: '🧸', desc: '关注儿童常见病、发热咳嗽与日常健康管理。' },
    { id: 4, name: '皮肤科', icon: '🌿', desc: '提供常见皮肤问题诊治与社区随访建议。' },
    { id: 5, name: '妇科', icon: '🌸', desc: '覆盖女性常见健康咨询、检查与门诊服务。' },
    { id: 6, name: '口腔科', icon: '🦷', desc: '适用于牙痛、洁治、基础口腔检查等场景。' }
  );
};

const loadDoctors = () => {
  request.get('/doctor/selectPage', {
    params: {
      pageNum: 1,
      pageSize: 10,
      name: null
    }
  }).then(res => {
    if (res.code === '200' && res.data?.list) {
      doctorList.length = 0;
      const activeDoctors = res.data.list.filter(item => item.status === 1);
      activeDoctors.forEach(item => {
        doctorList.push({
          id: item.id,
          name: item.name,
          department: item.department,
          title: item.title,
          desc: item.intro || '该医生经验丰富，擅长社区常见病与连续健康管理服务。',
          avatar: item.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
        });
      });
      if (doctorList.length === 0) {
        ElMessage.warning('暂无在职医生信息');
      }
    } else {
      ElMessage.warning('暂无医生数据');
    }
  }).catch(err => {
    console.error('医生列表加载失败：', err);
    ElMessage.error('医生数据加载失败，请稍后重试');
    doctorList.push(
      { id: 1, name: '赵文静', department: '妇科', title: '副主任医师', desc: '长期从事妇科门诊与基层健康随访工作。', avatar: 'https://source.unsplash.com/random/160x160?female-doctor' },
      { id: 2, name: '刘德远', department: '皮肤科', title: '副主任医师', desc: '擅长常见皮肤问题诊疗与社区连续管理。', avatar: 'https://source.unsplash.com/random/160x160?male-doctor' },
      { id: 3, name: '李娜', department: '儿科', title: '主治医师', desc: '关注儿童常见病、呼吸道问题与家庭健康指导。', avatar: 'https://source.unsplash.com/random/160x160?pediatrician' }
    );
  });
};

const loadNews = () => {
  newsList.length = 0;
  newsList.push(
    { id: 1, title: '春季儿童常见传染病预防提醒', desc: '春季是流感、手足口病等高发阶段，建议勤洗手、常通风，若出现发热或皮疹应及时就诊。', time: '2026-03-01', img: ZX1 },
    { id: 2, title: '65 岁以上老年人体检服务开启', desc: '社区卫生服务中心提供血压、血糖、心电图等基础检查项目，可提前预约后到院办理。', time: '2026-02-25', img: ZX2 },
    { id: 3, title: '家庭医生签约服务持续开放', desc: '签约后可享受慢病管理、健康咨询与连续随访等社区医疗服务，适合长期健康管理人群。', time: '2026-02-20', img: ZX3 },
    { id: 4, title: '高血压患者居家护理注意事项', desc: '建议规律服药、控制盐分摄入、持续监测血压，并按时到社区医院复查评估。', time: '2026-02-15', img: ZX4 },
    { id: 5, title: '社区疫苗接种安排更新', desc: '包含儿童疫苗、流感疫苗与 HPV 疫苗等接种时段，建议提前电话咨询并预约。', time: '2026-02-10', img: ZX5 }
  );
};

const submitAppointment = () => {
  appointmentFormRef.value.validate((valid) => {
    if (!valid) return;
    if (!validateTimeIsValid()) return;

    const userStr = localStorage.getItem('system-user');
    if (!userStr) {
      ElMessage.warning('请先登录再预约');
      router.push('/login');
      return;
    }

    const user = JSON.parse(userStr);
    if (!user.id) {
      ElMessage.warning('用户信息异常，请重新登录');
      return;
    }

    const date = new Date(appointmentForm.date);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const formattedDate = `${year}-${month}-${day}`;

    request.post('/appointment/add', {
      ...appointmentForm,
      userId: user.id,
      date: formattedDate
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('预约成功，我们会尽快与您联系');
        appointmentFormRef.value.resetFields();
      } else {
        ElMessage.error(res.msg || '预约失败，请稍后重试');
      }
    }).catch(err => {
      ElMessage.error('预约提交失败，请稍后重试');
      console.error('预约失败：', err);
    });
  });
};

const timeOptions = [
  { label: '上午 8:00-10:00', value: '08:00-10:00', endHour: 10 },
  { label: '上午 10:00-12:00', value: '10:00-12:00', endHour: 12 },
  { label: '下午 14:00-16:00', value: '14:00-16:00', endHour: 16 },
  { label: '下午 16:00-18:00', value: '16:00-18:00', endHour: 18 }
];

const isTimePassed = (option) => {
  if (!appointmentForm.date) return false;
  const selectedDate = new Date(appointmentForm.date);
  const today = new Date();
  if (selectedDate.toDateString() !== today.toDateString()) return false;
  const currentHour = new Date().getHours();
  const currentMinute = new Date().getMinutes();
  return currentHour > option.endHour || (currentHour === option.endHour && currentMinute > 0);
};

const handleDisabledTimeClick = (option) => {
  if (isTimePassed(option)) {
    ElMessage.warning('该预约时段已过，请选择其他时段');
  }
};

const validateTimeIsValid = () => {
  if (!appointmentForm.time) return false;
  const currentTimeOption = timeOptions.find(item => item.value === appointmentForm.time);
  if (currentTimeOption && isTimePassed(currentTimeOption)) {
    ElMessage.error('所选预约时段已过，请重新选择');
    return false;
  }
  return true;
};

const handleEntryClick = (item) => {
  if (item.path.includes('#')) {
    const [path, anchor] = item.path.split('#');
    if (path === '/front/home') {
      document.getElementById(anchor)?.scrollIntoView({ behavior: 'smooth' });
    } else {
      router.push(path).then(() => {
        setTimeout(() => {
          document.getElementById(anchor)?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
      });
    }
  } else {
    router.push(item.path).catch(err => {
      console.error('路由跳转失败:', err);
      ElMessage.warning('跳转失败，请稍后重试');
    });
  }
};

const goToAppointment = () => {
  document.getElementById('appointment')?.scrollIntoView({ behavior: 'smooth' });
};

const chooseDoctor = (doctorId) => {
  appointmentForm.doctorId = doctorId;
  document.getElementById('appointment')?.scrollIntoView({ behavior: 'smooth' });
};

const disabledPastDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7;
};
</script>

<style scoped>
.home-page {
  --text-main: #16332e;
  --text-soft: #607871;
  --teal: #16957b;
  --teal-deep: #0f6d5a;
  --mint: #d9f1e8;
  --shadow: 0 24px 60px rgba(12, 76, 62, 0.08);
  background: radial-gradient(circle at top left, rgba(75, 192, 153, 0.12), transparent 24%), linear-gradient(180deg, #f6fcfa 0%, #eef8f4 100%);
  color: var(--text-main);
}

.hero-section,
.quick-entry-section,
.section-block,
.about-section,
.appointment-section {
  width: min(1240px, calc(100% - 40px));
  margin: 0 auto;
}

.hero-section {
  padding: 28px 0 48px;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.72fr) minmax(0, 1.28fr);
  gap: 22px;
  align-items: center;
}

.hero-copy {
  padding: 12px 0 12px 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-kicker,
.section-kicker {
  margin: 0 0 10px;
  color: var(--teal);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  font-family: "STZhongsong", "SimSun", serif;
  font-size: clamp(36px, 4.4vw, 64px);
  line-height: 1.12;
}

.hero-desc {
  max-width: 430px;
  margin: 24px 0 0;
  color: var(--text-soft);
  font-size: 17px;
  line-height: 1.95;
}

.hero-actions {
  display: flex;
  gap: 14px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.hero-btn {
  min-width: 144px;
  height: 50px;
  border-radius: 16px;
  font-weight: 700;
}

.hero-btn-primary {
  border: none;
  background: linear-gradient(120deg, var(--teal), #2bb38e);
  box-shadow: 0 18px 32px rgba(22, 149, 123, 0.22);
}

.hero-btn-secondary {
  border: 1px solid rgba(22, 149, 123, 0.2);
  color: var(--teal-deep);
  background: rgba(255, 255, 255, 0.84);
}

.hero-mini-note {
  margin-top: 26px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6a837b;
  font-size: 14px;
  line-height: 1.8;
}

.note-dot {
  width: 10px;
  height: 10px;
  flex-shrink: 0;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--teal), #4dc6a0);
  box-shadow: 0 0 0 6px rgba(22, 149, 123, 0.1);
}

.hero-visual {
  display: block;
  width: 100%;
  min-width: 0;
}

.hero-panel {
  width: 100%;
}

.hero-carousel-panel {
  width: 100%;
  min-width: 0;
}

.hero-panel,
.about-copy-card,
.about-gallery,
.appointment-form-wrap,
.appointment-tip-card {
  border-radius: 28px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.55);
  background: rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow);
}

.hero-carousel-panel,
.about-gallery {
  padding: 16px;
}

.hero-carousel-panel {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(246, 252, 249, 0.92)),
    radial-gradient(circle at top right, rgba(22, 149, 123, 0.12), transparent 26%);
}

.panel-badge {
  position: absolute;
  top: 18px;
  left: 18px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  padding: 0 14px;
  height: 34px;
  border-radius: 999px;
  background: rgba(10, 37, 31, 0.5);
  color: #fff;
  font-size: 12px;
  letter-spacing: 0.14em;
}

.panel-badge.soft {
  position: static;
  width: fit-content;
  background: var(--mint);
  color: var(--teal-deep);
  margin-bottom: 16px;
}

.hero-carousel {
  border-radius: 22px;
  overflow: hidden;
  width: 100%;
}

.hero-carousel :deep(.el-carousel__container) {
  width: 100%;
}

.hero-slide {
  position: relative;
  width: 100%;
  height: 560px;
  border-radius: 22px;
}

.hero-slide {
  overflow: hidden;
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.16), transparent 32%),
    linear-gradient(135deg, #0c7cab 0%, #0f9b8e 100%);
}

.hero-slide-image {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  object-position: center;
}

.about-slide {
  position: relative;
  width: 100%;
  height: 460px;
  border-radius: 22px;
  background-size: cover;
  background-position: center;
}

.hero-slide-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(7, 23, 20, 0.08), rgba(7, 23, 20, 0.72)),
    linear-gradient(90deg, rgba(7, 23, 20, 0.18), transparent 40%, rgba(7, 23, 20, 0.16));
}

.hero-slide-content {
  position: absolute;
  left: 34px;
  right: 34px;
  bottom: 34px;
  z-index: 1;
  color: #fff;
  max-width: 68%;
}

.hero-slide-content p {
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(236, 255, 248, 0.78);
}

.hero-slide-content h3 {
  margin: 0;
  font-size: 32px;
  line-height: 1.25;
}

.hero-slide-content span {
  display: block;
  margin-top: 10px;
  max-width: 560px;
  line-height: 1.8;
  color: rgba(245, 255, 251, 0.86);
}

.quick-entry-section {
  padding: 10px 0 20px;
}

.section-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 24px;
}

.section-head.compact {
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 0;
  font-size: 34px;
  line-height: 1.2;
  font-family: "STZhongsong", "SimSun", serif;
}

.section-head.light h2,
.section-head.light .section-kicker {
  color: #effff9;
}

.section-hint {
  color: #779086;
  font-size: 14px;
}

.quick-entry-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.entry-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(22, 149, 123, 0.08);
  box-shadow: 0 18px 28px rgba(17, 72, 58, 0.05);
  cursor: pointer;
  transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
}

.entry-card:hover,
.doctor-card:hover {
  transform: translateY(-4px);
}

.entry-icon {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--teal);
  background: linear-gradient(145deg, #edf8f3, #d7efe6);
}

.entry-meta strong {
  display: block;
  font-size: 16px;
  margin-bottom: 6px;
}

.entry-meta span {
  color: #748a83;
  font-size: 13px;
}

.about-section,
.section-block {
  padding: 40px 0 0;
}

.about-layout {
  display: grid;
  grid-template-columns: 0.92fr 1.08fr;
  gap: 24px;
}

.about-copy-card {
  padding: 32px;
}

.about-label {
  margin: 0 0 12px;
  color: var(--teal);
  font-size: 13px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.about-copy-card h3 {
  margin: 0;
  font-size: 28px;
  line-height: 1.45;
}

.about-copy-card p {
  margin: 16px 0 0;
  color: var(--text-soft);
  line-height: 1.9;
}

.department-grid,
.doctor-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.department-card,
.doctor-card,
.news-card {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(22, 149, 123, 0.08);
  box-shadow: 0 20px 32px rgba(17, 72, 58, 0.05);
}

.department-card {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 24px;
}

.department-icon {
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #def2ea, #cbe8de);
  color: var(--teal-deep);
  font-size: 26px;
  line-height: 1;
}

.department-text h4,
.doctor-basic h3 {
  margin: 0;
}

.department-text p,
.doctor-basic p,
.doctor-desc,
.news-body p {
  color: var(--text-soft);
  line-height: 1.8;
}

.department-text p {
  margin: 10px 0 0;
}

.empty-card {
  grid-column: 1 / -1;
  padding: 40px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.8);
  text-align: center;
  color: #7d918b;
}

.doctor-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.28s ease, box-shadow 0.28s ease;
}

.doctor-top {
  display: flex;
  align-items: center;
  gap: 16px;
}

.doctor-avatar {
  border: 4px solid #eef9f3;
}

.doctor-basic p {
  margin: 8px 0 0;
}

.doctor-desc {
  margin: 18px 0;
  min-height: 76px;
}

.doctor-action {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  margin-top: auto;
  color: var(--teal);
  font-weight: 700;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.news-cover {
  height: 220px;
  overflow: hidden;
}

.news-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease;
}

.news-card:hover .news-img {
  transform: scale(1.04);
}

.news-body {
  padding: 22px;
}

.news-date {
  display: inline-block;
  margin-bottom: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #eef9f4;
  color: var(--teal-deep);
  font-size: 12px;
  font-weight: 700;
}

.news-body h4 {
  margin: 0;
  font-size: 22px;
  line-height: 1.45;
}

.appointment-section {
  padding: 44px 0 56px;
}

.appointment-layout {
  display: grid;
  grid-template-columns: 0.42fr 0.58fr;
  gap: 22px;
}

.appointment-tip-card {
  height: 100%;
  padding: 28px;
  background: linear-gradient(160deg, #106d5b, #17967d);
  color: #fff;
}

.appointment-tip-card h3 {
  margin: 0 0 18px;
  font-size: 26px;
}

.appointment-tip-card ul {
  margin: 0;
  padding-left: 18px;
  line-height: 2;
  color: rgba(244, 255, 251, 0.9);
}

.appointment-form-wrap {
  padding: 28px 28px 12px;
}

.field-control {
  width: 100%;
}

.appointment-form :deep(.el-input__wrapper),
.appointment-form :deep(.el-select__wrapper) {
  min-height: 48px;
  border-radius: 16px;
  box-shadow: none;
  background: #f8fcfa;
}

.appointment-form :deep(.el-input__wrapper.is-focus),
.appointment-form :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 4px rgba(22, 149, 123, 0.08);
}

.appointment-btn-wrapper {
  margin-top: 26px;
}

.appointment-btn-wrapper :deep(.el-form-item__content) {
  justify-content: flex-start;
}

.submit-btn {
  height: 50px;
  padding: 0 28px;
  border-radius: 16px;
  border: none;
  font-weight: 700;
  background: linear-gradient(120deg, var(--teal), #2ab18d);
  box-shadow: 0 18px 30px rgba(22, 149, 123, 0.22);
}

.contact-section {
  margin-top: 26px;
  padding: 48px 0 56px;
  background: linear-gradient(135deg, #0f5f51, #0b3f39);
}

.inner-head,
.contact-grid,
.copyright-text {
  width: min(1240px, calc(100% - 40px));
  margin-left: auto;
  margin-right: auto;
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.contact-card {
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(231, 255, 245, 0.1);
  color: #fff;
}

.contact-card span {
  display: block;
  margin-bottom: 10px;
  color: rgba(215, 247, 235, 0.78);
  font-size: 13px;
  letter-spacing: 0.14em;
}

.contact-card strong {
  display: block;
  font-size: 24px;
  line-height: 1.4;
}

.contact-card p {
  margin: 12px 0 0;
  color: rgba(235, 255, 248, 0.82);
  line-height: 1.8;
}

.copyright-text {
  margin-top: 28px;
  color: rgba(225, 251, 241, 0.72);
  text-align: center;
  font-size: 13px;
}

@media (max-width: 1100px) {
  .hero-grid,
  .about-layout,
  .appointment-layout {
    grid-template-columns: 1fr;
  }

  .quick-entry-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .department-grid,
  .doctor-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-section,
  .quick-entry-section,
  .section-block,
  .about-section,
  .appointment-section,
  .inner-head,
  .contact-grid,
  .copyright-text {
    width: min(1240px, calc(100% - 24px));
  }

  .hero-copy {
    padding-right: 0;
  }

  .hero-mini-note {
    align-items: flex-start;
  }

  .hero-stats,
  .quick-entry-grid,
  .department-grid,
  .doctor-grid,
  .news-grid,
  .contact-grid {
    grid-template-columns: 1fr;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-head h2 {
    font-size: 28px;
  }

  .hero-copy h1 {
    font-size: 34px;
  }

  .hero-slide {
    height: 380px;
  }

  .about-slide {
    height: 320px;
  }

  .hero-slide-content {
    left: 20px;
    right: 20px;
    bottom: 20px;
    max-width: none;
  }

  .hero-slide-content h3,
  .news-body h4 {
    font-size: 24px;
  }

  .about-copy-card,
  .appointment-form-wrap,
  .appointment-tip-card {
    padding: 22px;
  }
}
</style>
