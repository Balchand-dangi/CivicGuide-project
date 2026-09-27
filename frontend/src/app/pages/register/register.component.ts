import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent implements OnInit, OnDestroy {
  registerForm: FormGroup;
  isLoading = false;
  private roleSub?: Subscription;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router,
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
      role: ['USER', Validators.required],

      // Mentor fields — validators applied dynamically
      qualification: [''],
      specialization: [''],
      experienceYears: [''],
      languages: [''],
      bio: [''],
    });
  }

  get f() { return this.registerForm.controls; }

  get isMentor(): boolean {
    return this.registerForm.get('role')?.value === 'MENTOR';
  }

  ngOnInit(): void {
    // Use valueChanges so role changes triggered by ANY input method (keyboard, paste, JS) work correctly.
    this.roleSub = this.registerForm.get('role')!.valueChanges.subscribe(role => {
      const mentorFields = ['qualification', 'specialization', 'experienceYears', 'languages', 'bio'];
      if (role === 'MENTOR') {
        mentorFields.forEach(field => {
          this.registerForm.get(field)?.setValidators(Validators.required);
          this.registerForm.get(field)?.updateValueAndValidity();
        });
      } else {
        mentorFields.forEach(field => {
          this.registerForm.get(field)?.clearValidators();
          this.registerForm.get(field)?.updateValueAndValidity();
        });
      }
    });
  }

  ngOnDestroy(): void {
    this.roleSub?.unsubscribe();
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const formData = this.registerForm.value;

    if (formData.password !== formData.confirmPassword) {
      this.toastService.error('Passwords do not match.');
      return;
    }

    this.isLoading = true;

    this.authService.register(formData).subscribe({
      next: () => {
        this.isLoading = false;
        this.toastService.success('Account created! Please log in.');
        this.router.navigate(['/login']);
        this.registerForm.reset({ role: 'USER' });
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 409) {
          this.toastService.error('This email is already registered.');
        } else if (err.status === 400) {
          const msg = err.error?.message || 'Please check your registration details.';
          this.toastService.error(msg);
        } else {
          this.toastService.error('Something went wrong. Please try again.');
        }
      },
    });
  }
}
