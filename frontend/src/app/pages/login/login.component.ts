import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router,
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        // AuthService.login() taps to refresh the shared user state, so the
        // navbar already knows the user before we navigate.
        this.authService.getCurrentUser().subscribe({
          next: (user) => {
            this.isLoading = false;
            switch (user.role) {
              case 'USER': this.router.navigate(['/dashboard']); break;
              case 'MENTOR': this.router.navigate(['/mentor/dashboard']); break;
              case 'ADMIN': this.router.navigate(['/admin/dashboard']); break;
              default: this.router.navigate(['/login']);
            }
          },
          error: () => { this.isLoading = false; this.router.navigate(['/login']); },
        });
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.toastService.error('Invalid email or password.');
        } else if (err.status === 0) {
          this.toastService.error('Cannot connect to the server. Is Spring Boot running?');
        } else {
          this.toastService.error('Login failed. Please try again.');
        }
      },
    });
  }
}
