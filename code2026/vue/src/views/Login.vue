<template>
  <div class="auth-shell login-theme">
    <div class="auth-bg-grid"></div>
    <div class="auth-glow auth-glow-one"></div>
    <div class="auth-glow auth-glow-two"></div>

    <section class="auth-layout">
      <aside class="auth-aside">
        <img src="@/assets/imgs/logo.png" alt="社区医院 logo" class="brand-logo">
        <p class="eyebrow">Community Health Service</p>
        <h1>一站式整合社区医疗服务，全方位守护居民健康生活。</h1>
        <p class="aside-copy">
          面向用户、医生与管理者的统一工作台，帮助社区医院更稳定地完成基本功能和稳定运行。
        </p>

        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-tag">01</span>
            <div>
              <strong>统一身份入口</strong>
              <p>按角色快速进入用户端、医生端和管理后台。</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-tag">02</span>
            <div>
              <strong>业务清晰可追踪</strong>
              <p>预约、诊疗记录、慢病管理等流程集中处理。</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-tag">03</span>
            <div>
              <strong>适合基层服务场景</strong>
              <p>强调稳定、易用与高频业务的快速触达。</p>
            </div>
          </div>
        </div>
        
      </aside>

      <main class="auth-panel">
        <div class="panel-topline"></div>
        <div class="panel-header">
          <p class="panel-kicker">账号登录</p>
          <h2>欢迎回来</h2>
          <p class="panel-subtitle">请输入账号信息，进入社区医院管理系统。</p>
        </div>

        <el-form :model="data.form" ref="formRef" :rules="data.rules" class="auth-form">
          <div class="field-group">
            <label>账号</label>
            <el-form-item prop="username">
              <el-input
                v-model="data.form.username"
                :prefix-icon="User"
                size="large"
                placeholder="请输入登录账号"
                class="auth-input"
              />
            </el-form-item>
          </div>

          <div class="field-group">
            <label>密码</label>
            <el-form-item prop="password">
              <el-input
                v-model="data.form.password"
                :prefix-icon="Lock"
                size="large"
                type="password"
                show-password
                placeholder="请输入登录密码"
                class="auth-input"
              />
            </el-form-item>
          </div>

          <div class="field-group">
            <label>登录身份</label>
            <el-form-item prop="role">
              <el-select v-model="data.form.role" size="large" class="auth-select" popper-class="auth-popper">
                <el-option value="普通用户" label="普通用户"></el-option>
                <el-option value="医务人员" label="医务人员"></el-option>
                <el-option value="管理员" label="管理员"></el-option>
              </el-select>
            </el-form-item>
          </div>

          <el-form-item class="submit-row">
            <el-button
              size="large"
              type="primary"
              class="submit-btn"
              @click="login"
              :loading="data.loading"
            >
              进入系统
            </el-button>
          </el-form-item>
        </el-form>

        <div class="panel-note">
          <span>还没有账号？</span>
          <router-link to="/register" class="switch-link">立即注册</router-link>
        </div>
      </main>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

const data = reactive({
  form: { role: '普通用户' },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
    ],
  },
  loading: false
})

const formRef = ref()

const login = () => {
  formRef.value.validate((valid => {
    if (valid) {
      data.loading = true
      request.post('/login', data.form).then(res => {
        data.loading = false
        if (res.code === '200') {
          ElMessage.success("登录成功")
          localStorage.setItem('system-user', JSON.stringify(res.data))
          if (res.data.role === '管理员') {
            router.push('/manager/home')
          } else if (res.data.role === '医务人员') {
            router.push('/doctor/home')
          } else {
            router.push('/front/home')
          }
        } else {
          ElMessage.error(res.msg)
        }
      }).catch(error => {
        data.loading = false
        console.error(error)
      })
    }
  })).catch(error => {
    console.error(error)
  })
}
</script>

<style scoped>
.auth-shell {
  --bg: #081a29;
  --bg-soft: #0f2b3d;
  --surface: rgba(7, 21, 33, 0.78);
  --surface-strong: rgba(255, 255, 255, 0.92);
  --line: rgba(150, 208, 255, 0.18);
  --line-strong: rgba(39, 151, 255, 0.18);
  --text-main: #102235;
  --text-soft: #587087;
  --text-on-dark: rgba(241, 248, 255, 0.88);
  --accent: #2e8fff;
  --accent-strong: #0fd0cc;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(41, 148, 255, 0.24), transparent 34%),
    radial-gradient(circle at bottom right, rgba(15, 208, 204, 0.2), transparent 28%),
    linear-gradient(135deg, var(--bg) 0%, var(--bg-soft) 52%, #06131f 100%);
  font-family: "Microsoft YaHei", "PingFang SC", "Segoe UI", sans-serif;
}

.auth-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(138, 199, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(138, 199, 255, 0.06) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: radial-gradient(circle at center, black 48%, transparent 88%);
  pointer-events: none;
}

.auth-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(10px);
  pointer-events: none;
}

.auth-glow-one {
  width: 340px;
  height: 340px;
  top: -120px;
  right: 12%;
  background: rgba(46, 143, 255, 0.16);
}

.auth-glow-two {
  width: 280px;
  height: 280px;
  bottom: -80px;
  left: 8%;
  background: rgba(15, 208, 204, 0.18);
}

.auth-layout {
  position: relative;
  z-index: 1;
  width: min(1180px, 100%);
  min-height: 720px;
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  border-radius: 32px;
  overflow: hidden;
  border: 1px solid rgba(170, 221, 255, 0.12);
  box-shadow: 0 32px 80px rgba(2, 13, 24, 0.45);
  background: rgba(8, 24, 38, 0.56);
  backdrop-filter: blur(16px);
}

.auth-aside {
  position: relative;
  padding: 56px 54px 42px;
  color: #fff;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.05), transparent 45%),
    linear-gradient(160deg, rgba(9, 31, 51, 0.88), rgba(8, 24, 38, 0.78));
}

.brand-pill {
  display: inline-flex;
  align-items: center;
  height: 34px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid rgba(174, 227, 255, 0.18);
  background: rgba(255, 255, 255, 0.08);
  font-size: 13px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.brand-logo {
  width: 78px;
  height: 78px;
  margin-top: 34px;
  margin-left: auto;
  margin-right: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  object-fit: contain;
  background: rgba(255, 255, 255, 0.94);
  padding: 12px;
  box-shadow: 0 20px 36px rgba(0, 0, 0, 0.18);
}

.eyebrow {
  margin: 28px 0 12px;
  color: rgba(169, 219, 255, 0.76);
  font-size: 13px;
  letter-spacing: 0.28em;
  text-transform: uppercase;
}

.auth-aside h1 {
  margin: 0;
  max-width: 620px;
  font-family: "STZhongsong", "SimSun", serif;
  font-size: clamp(34px, 4vw, 52px);
  line-height: 1.18;
  letter-spacing: 0.02em;
  color: #f5fbff;
}

.aside-copy {
  max-width: 540px;
  margin: 22px 0 0;
  font-size: 16px;
  line-height: 1.85;
  color: var(--text-on-dark);
}

.feature-list {
  margin-top: 34px;
  display: grid;
  gap: 16px;
}

.feature-item {
  display: grid;
  grid-template-columns: 60px 1fr;
  gap: 16px;
  padding: 18px 18px 18px 16px;
  border: 1px solid rgba(169, 219, 255, 0.12);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.05);
}

.feature-item strong {
  display: block;
  margin-bottom: 6px;
  font-size: 16px;
  color: #f8fcff;
}

.feature-item p {
  margin: 0;
  color: rgba(233, 244, 255, 0.7);
  line-height: 1.7;
  font-size: 14px;
}

.feature-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(46, 143, 255, 0.9), rgba(15, 208, 204, 0.78));
  color: white;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.aside-footer {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.mini-stat {
  padding: 18px;
  border-radius: 20px;
  border: 1px solid rgba(169, 219, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
}

.mini-stat span {
  display: block;
  margin-bottom: 8px;
  color: rgba(169, 219, 255, 0.72);
  font-size: 12px;
  letter-spacing: 0.14em;
}

.mini-stat strong {
  color: #ffffff;
  line-height: 1.6;
  font-size: 16px;
}

.auth-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 54px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(247, 251, 255, 0.94));
}

.panel-topline {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 6px;
  background: linear-gradient(90deg, var(--accent), var(--accent-strong));
}

.panel-header {
  margin-bottom: 28px;
}

.panel-kicker {
  margin: 0 0 10px;
  color: var(--accent);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.panel-header h2 {
  margin: 0;
  color: var(--text-main);
  font-family: "STZhongsong", "SimSun", serif;
  font-size: 36px;
  line-height: 1.2;
}

.panel-subtitle {
  margin: 12px 0 0;
  color: var(--text-soft);
  line-height: 1.8;
  font-size: 15px;
}

.auth-form {
  margin-top: 10px;
}

.field-group {
  margin-bottom: 18px;
}

.field-group label {
  display: block;
  margin-bottom: 10px;
  color: #365064;
  font-size: 14px;
  font-weight: 600;
}

.auth-input,
.auth-select {
  width: 100%;
}

.auth-input :deep(.el-input__wrapper),
.auth-select :deep(.el-input__wrapper) {
  min-height: 54px;
  border-radius: 18px;
  padding: 0 16px;
  box-shadow: none;
  background: rgba(246, 250, 255, 0.96);
  border: 1px solid transparent;
  transition: all 0.28s ease;
}

.auth-input :deep(.el-input__wrapper:hover),
.auth-select :deep(.el-input__wrapper:hover) {
  background: #fff;
  border-color: var(--line);
  box-shadow: 0 12px 30px rgba(24, 77, 128, 0.08);
}

.auth-input :deep(.el-input__wrapper.is-focus),
.auth-select :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(46, 143, 255, 0.1);
}

.auth-input :deep(.el-input__prefix-inner),
.auth-select :deep(.el-input__prefix-inner) {
  color: #7b93a6;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.submit-row {
  margin-top: 26px;
}

.submit-row :deep(.el-form-item__content) {
  display: block;
}

.submit-btn {
  width: 100%;
  height: 54px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(120deg, var(--accent) 0%, #4ca8ff 44%, var(--accent-strong) 100%);
  box-shadow: 0 18px 30px rgba(46, 143, 255, 0.24);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 24px 34px rgba(46, 143, 255, 0.28);
}

.panel-note {
  margin-top: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #688095;
  font-size: 14px;
}

.switch-link {
  color: var(--accent);
  font-weight: 700;
}

.switch-link:hover {
  color: #106fd8;
}

@media (max-width: 1080px) {
  .auth-layout {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .auth-aside {
    padding-bottom: 28px;
  }

  .auth-panel {
    padding-top: 42px;
  }
}

@media (max-width: 640px) {
  .auth-shell {
    padding: 16px;
  }

  .auth-layout {
    border-radius: 24px;
  }

  .auth-aside,
  .auth-panel {
    padding: 28px 22px;
  }

  .auth-aside h1 {
    font-size: 30px;
  }

  .feature-item {
    grid-template-columns: 1fr;
  }

  .aside-footer {
    grid-template-columns: 1fr;
  }

  .panel-header h2 {
    font-size: 30px;
  }
}
</style>
