// routes.ts

const routes = {
  dashboard: {
    path: '/dashboard',
    permissions: ['user', 'admin'],
    get component() {
      return import('../components/Dashboard');
    },
  },
  adminDashboard: {
    path: '/admin-dashboard',
    permissions: ['admin'],
    get component() {
      return import('../components/AdminDashboard');
    },
  },
  // Additional routes
  someOtherRoute: {
    path: '/some-other-route',
    permissions: ['user'],
    get component() {
      return import('../components/SomeOtherComponent');
    },
  },
  anotherRoute: {
    path: '/another-route',
    permissions: ['admin', 'user'],
    get component() {
      return import('../components/AnotherComponent');
    },
  },
};

export default routes;