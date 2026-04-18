import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/manager/home',
      children: [
        { path: 'home', component: () => import('@/views/manager/Home.vue')},
        { path: 'admin', component: () => import('@/views/manager/Admin.vue')},
        { path: 'user', component: () => import('@/views/manager/User.vue')},
        { path: 'person', component: () => import('@/views/manager/Person.vue')},
        { path: 'password', component: () => import('@/views/manager/Password.vue')},
        { path: 'category', component: () => import('@/views/manager/Category.vue')},
        { path: 'goods', component: () => import('@/views/manager/Goods.vue')},
        { path: 'carousel', component: () => import('@/views/manager/Carousel.vue')},
        { path: 'appointment', component: () => import('@/views/manager/Appointment.vue')},
        { path: 'doctor', component: () => import('@/views/manager/Doctor.vue')},
        { path: 'assessment', component: () => import('@/views/manager/HealthAssessmentQuestion.vue')},
        { path: 'inquiry-record', component: () => import('@/views/manager/InquiryRecord.vue') },
        { path: 'consultation', component: () => import('@/views/manager/InquiryRecord.vue') },
      ]
    },
    {
      path: '/front',
      component: () => import('@/views/Front.vue'),
      redirect: '/front/home',
      children: [
        { path: 'home', component: () => import('@/views/front/Home.vue')},
        { path: 'person', component: () => import('@/views/front/Person.vue')},
        { path: 'password', component: () => import('@/views/front/Password.vue')},
        { path: 'my-records', component: () => import('@/views/front/MyRecord.vue')},
        { path: 'health-record', component: () => import('@/views/front/HealthRecord.vue')},
        { path: 'health-assessment', component: () => import('@/views/front/HealthAssessment.vue')},
        { path: 'health-assessment-record', component: () => import('@/views/front/HealthAssessmentRecord.vue')},
        { path: 'consultation', component: () => import('@/views/front/Consultation.vue') }, // 问诊记录
        { path: 'consultation-add', component: () => import('@/views/front/ConsultationAdd.vue') }
      ]
    },
    {
      path: '/doctor',
      component: () => import('@/views/Doctor.vue'),
      redirect: '/doctor/home',
      children: [
        { path: 'home', component: () => import('@/views/doctor/Home.vue')},
        { path: 'appointment', component: () => import('@/views/doctor/Appointment.vue')},
        { path: 'person', component: () => import('@/views/doctor/Person.vue')},
        { path: 'password', component: () => import('@/views/doctor/Password.vue')},
        { path: 'record', component: () => import('@/views/doctor/Record.vue')},
        { path: 'consultation', component: () => import('@/views/doctor/Consultation.vue')},
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') }
  ]
})

export default router