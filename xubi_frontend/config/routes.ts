export default [
  {path: '/',redirect: '/add_chart'},
  { name: '智能分析',path: '/add_chart',icon:'barChart',
    component: './AddChart'},
  { name: '我的图表',path: '/my_chart',icon:'pieChart',
    component: './MyChart'},
  {
    path: '/admin',
    icon: 'crown',
    access: 'canAdmin',
    name:'管理员页面',
    routes: [
      {path: '/admin', redirect: '/admin/sub-page'},
      {path: '/admin/sub-page', component: './Admin'},
    ],
  },
  {path: '/', redirect: '/welcome'},
  {path: '*', layout: false, component: './404'},
  {
    path: '/user',
    layout: false,
    routes: [
      {path: '/user/login', layout: false, name: '登录', component: './user/login'},
      {path: '/user', redirect: '/user/login'},
      {
        name: '注册结果',
        icon: 'smile',
        path: '/user/register-result',
        component: './user/register-result',
      },
      {name: '注册', icon: 'smile', path: '/user/register', component: './user/register'},
      {component: '404', path: '/user/*'},
    ],
  },

  {path: '/', redirect: '/dashboard/analysis'},
  {component: '404', path: '/*'},
];
