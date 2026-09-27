import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, UserResponse } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent implements OnInit {
  user: UserResponse | null = null;

  constructor(
    private auth: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    // Subscribe to the shared reactive user state — updates automatically
    // whenever login() or logout() is called anywhere in the app.
    this.auth.currentUser$.subscribe((user) => (this.user = user));
    // Seed the initial state from the backend (handles page refresh).
    this.auth.refreshCurrentUser();
  }

  dashboardPath(): string {
    if (this.user?.role === 'MENTOR') return '/mentor/dashboard';
    if (this.user?.role === 'ADMIN') return '/admin/dashboard';
    return '/dashboard';
  }

  logout(): void {
    this.auth.logout().subscribe({
      next: () => this.router.navigate(['/']),
      error: () => this.router.navigate(['/']),
    });
  }
}
