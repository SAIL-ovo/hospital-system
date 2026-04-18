<template>
  <div class="auth-shell register-theme">
    <div class="auth-bg-grid"></div>
    <div class="auth-glow auth-glow-one"></div>
    <div class="auth-glow auth-glow-two"></div>

    <section class="auth-layout">
      <aside class="auth-aside">
        <img src="@/assets/imgs/logo.png" alt="社区医院 logo" class="brand-logo">
        <p class="eyebrow">Health Access Account</p>
        <h1>一站式整合社区医疗服务，全方位守护居民健康生活。</h1>
        <p class="aside-copy">
          注册后可进入用户端，查看个人信息、预约记录、健康档案与线上问诊相关服务，守护您的健康生活。
        </p>

        <div class="progress-card">
          <div class="progress-head">
            <span>开通流程</span>
            <strong>3 个步骤</strong>
          </div>
          <div class="progress-item">
            <em>01</em>
            <span>填写账号与密码</span>
          </div>
          <div class="progress-item">
            <em>02</em>
            <span>完成身份创建</span>
          </div>
          <div class="progress-item">
            <em>03</em>
            <span>返回登录页进入系统</span>
          </div>
        </div>

      </aside>

      <main class="auth-panel">
        <div class="panel-topline"></div>
        <div class="panel-header">
          <p class="panel-kicker">创建账号</p>
          <h2>欢迎加入</h2>
          <p class="panel-subtitle">填写基础信息后即可完成注册，随后使用新账号登录系统。</p>
        </div>

        <el-form :model="data.form" ref="formRef" :rules="data.rules" class="auth-form">
          <div class="field-group">
            <label>账号</label>
            <el-form-item prop="username">
              <el-input
                v-model="data.form.username"
                :prefix-icon="User"
                size="large"
                placeholder="请输入注册账号"
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
            <label>确认密码</label>
            <el-form-item prop="newPassword">
              <el-input
                v-model="data.form.newPassword"
                :prefix-icon="Lock"
                size="large"
                type="password"
                show-password
                placeholder="请再次输入密码"
                class="auth-input"
              />
            </el-form-item>
          </div>

          <el-form-item class="submit-row">
            <el-button
              size="large"
              type="primary"
              class="submit-btn"
              @click="register"
              :loading="data.loading"
            >
              完成注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="panel-note">
          <span>已有账号？</span>
          <router-link to="/login" class="switch-link">立即登录</router-link>
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

const formRef = ref()

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== data.form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const data = reactive({
  form: { role: '普通用户' },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
    ],
    newPassword: [
      { validator: validateConfirmPassword, trigger: 'blur' },
    ],
  },
  loading: false
})

const register = () => {
  formRef.value.validate((valid => {
    if (valid) {
      data.loading = true
      request.post('/register', data.form).then(res => {
        data.loading = false
        if (res.code === '200') {
          ElMessage.success("注册成功")
          router.push('/login')
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
  --bg: #081d1c;
  --bg-soft: #113734;
  --surface: rgba(7, 30, 28, 0.8);
  --surface-strong: rgba(251, 255, 253, 0.94);
  --line: rgba(129, 224, 190, 0.2);
  --line-strong: rgba(46, 170, 128, 0.22);
  --text-main: #102b25;
  --text-soft: #5a766e;
  --text-on-dark: rgba(239, 251, 246, 0.88);
  --accent: #1ea672;
  --accent-strong: #74d59a;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(116, 213, 154, 0.18), transparent 34%),
    radial-gradient(circle at bottom right, rgba(30, 166, 114, 0.22), transparent 30%),
    linear-gradient(135deg, var(--bg) 0%, var(--bg-soft) 55%, #071715 100%);
  font-family: "Microsoft YaHei", "PingFang SC", "Segoe UI", sans-serif;
}

.auth-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(182, 245, 215, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(182, 245, 215, 0.06) 1px, transparent 1px);
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
  right: 10%;
  background: rgba(116, 213, 154, 0.14);
}

.auth-glow-two {
  width: 280px;
  height: 280px;
  bottom: -90px;
  left: 10%;
  background: rgba(30, 166, 114, 0.18);
}

.auth-layout {
  position: relative;
  z-index: 1;
  width: min(1140px, 100%);
  min-height: 720px;
  display: grid;
  grid-template-columns: 1fr 0.94fr;
  border-radius: 32px;
  overflow: hidden;
  border: 1px solid rgba(189, 244, 218, 0.12);
  box-shadow: 0 32px 80px rgba(2, 18, 13, 0.42);
  background: rgba(8, 31, 24, 0.58);
  backdrop-filter: blur(16px);
}

.auth-aside {
  position: relative;
  padding: 56px 52px 42px;
  color: #fff;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.05), transparent 44%),
    linear-gradient(160deg, rgba(9, 42, 35, 0.88), rgba(8, 27, 24, 0.8));
}

.brand-pill {
  display: inline-flex;
  align-items: center;
  height: 34px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid rgba(193, 245, 219, 0.18);
  background: rgba(255, 255, 255, 0.08);
  font-size: 13px;
  letter-spacing: 0.18em;
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
  color: rgba(190, 244, 217, 0.78);
  font-size: 13px;
  letter-spacing: 0.28em;
  text-transform: uppercase;
}

.auth-aside h1 {
  margin: 0;
  max-width: 580px;
  font-family: "STZhongsong", "SimSun", serif;
  font-size: clamp(32px, 4vw, 48px);
  line-height: 1.2;
  color: #f5fff9;
}

.aside-copy {
  max-width: 520px;
  margin: 22px 0 0;
  font-size: 16px;
  line-height: 1.85;
  color: var(--text-on-dark);
}

.progress-card {
  margin-top: 34px;
  padding: 24px;
  border-radius: 24px;
  border: 1px solid rgba(193, 245, 219, 0.13);
  background: rgba(255, 255, 255, 0.05);
}

.progress-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.progress-head span {
  color: rgba(190, 244, 217, 0.76);
  font-size: 13px;
  letter-spacing: 0.18em;
}

.progress-head strong {
  font-size: 16px;
  color: #f6fff9;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-top: 1px solid rgba(193, 245, 219, 0.1);
  color: rgba(237, 251, 244, 0.86);
}

.progress-item:first-of-type {
  border-top: none;
  padding-top: 0;
}

.progress-item em {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  font-style: normal;
  font-weight: 700;
  color: #083226;
  background: linear-gradient(135deg, #b9f4d1, #74d59a);
}

.trust-row {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.trust-card {
  padding: 18px;
  border-radius: 20px;
  border: 1px solid rgba(193, 245, 219, 0.12);
  background: rgba(255, 255, 255, 0.04);
}

.trust-card span {
  display: block;
  margin-bottom: 8px;
  color: rgba(190, 244, 217, 0.72);
  font-size: 12px;
  letter-spacing: 0.14em;
}

.trust-card strong {
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
  background: linear-gradient(180deg, rgba(252, 255, 253, 0.96), rgba(248, 253, 250, 0.94));
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
  color: #39584f;
  font-size: 14px;
  font-weight: 600;
}

.auth-input {
  width: 100%;
}

.auth-input :deep(.el-input__wrapper) {
  min-height: 54px;
  border-radius: 18px;
  padding: 0 16px;
  box-shadow: none;
  background: rgba(247, 252, 249, 0.96);
  border: 1px solid transparent;
  transition: all 0.28s ease;
}

.auth-input :deep(.el-input__wrapper:hover) {
  background: #fff;
  border-color: var(--line);
  box-shadow: 0 12px 30px rgba(18, 85, 53, 0.08);
}

.auth-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(30, 166, 114, 0.1);
}

.auth-input :deep(.el-input__prefix-inner) {
  color: #779189;
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
  background: linear-gradient(120deg, var(--accent) 0%, #38b680 48%, var(--accent-strong) 100%);
  box-shadow: 0 18px 30px rgba(30, 166, 114, 0.24);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 24px 34px rgba(30, 166, 114, 0.28);
}

.panel-note {
  margin-top: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #6d857d;
  font-size: 14px;
}

.switch-link {
  color: var(--accent);
  font-weight: 700;
}

.switch-link:hover {
  color: #127854;
}

@media (max-width: 1080px) {
  .auth-layout {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .auth-aside {
    padding-bottom: 28px;
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
    font-size: 28px;
  }

  .trust-row {
    grid-template-columns: 1fr;
  }

  .panel-header h2 {
    font-size: 30px;
  }
}
</style>
