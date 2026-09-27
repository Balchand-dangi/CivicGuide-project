import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { roleGuard } from './guards/auth.guard';
import { UserDashboardComponent } from './pages/user-dashboard/user-dashboard.component';
import { MentorDashboardComponent } from './pages/mentor-dashboard/mentor-dashboard.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
  },
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'register',
    component: RegisterComponent,
  },
  {
    path: 'dashboard',
    component: UserDashboardComponent,
    canActivate: [roleGuard(['USER'])],
  },
  {
    path: 'mentor/dashboard',
    component: MentorDashboardComponent,
    canActivate: [roleGuard(['MENTOR'])],
  },
  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [roleGuard(['ADMIN'])],
  },
];
