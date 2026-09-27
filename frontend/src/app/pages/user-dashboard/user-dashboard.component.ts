import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService, UserResponse } from '../../services/auth.service';
import { CivicService, ServiceRequest, RequestStatus } from '../../services/civic.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css',
})
export class UserDashboardComponent implements OnInit {
  user: UserResponse | null = null;
  requests: ServiceRequest[] = [];
  loading = true;
  isSubmitting = false;

  requestForm: FormGroup;

  readonly services = [
    { category: 'Identity & Documents', name: 'Aadhaar Update', icon: 'bi-person-vcard' },
    { category: 'Travel', name: 'Passport Application', icon: 'bi-passport' },
    { category: 'Transport', name: 'Driving Licence', icon: 'bi-car-front' },
    { category: 'Certificates', name: 'Birth / Caste Certificate', icon: 'bi-file-earmark-text' },
    { category: 'Welfare', name: 'Government Scheme', icon: 'bi-bank' },
  ];

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private civic: CivicService,
    private toast: ToastService,
  ) {
    this.requestForm = this.fb.group({
      category: ['Identity & Documents', Validators.required],
      serviceName: ['Aadhaar Update', Validators.required],
      description: ['', [Validators.required, Validators.maxLength(1000)]],
    });
  }

  ngOnInit(): void {
    this.auth.getCurrentUser().subscribe({ next: user => (this.user = user) });
    this.loadRequests();
  }

  selectService(service: { category: string; name: string }): void {
    this.requestForm.patchValue({ category: service.category, serviceName: service.name });
    document.getElementById('new-request')?.scrollIntoView({ behavior: 'smooth' });
  }

  createRequest(): void {
    if (this.requestForm.invalid) {
      this.requestForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    this.civic.createRequest(this.requestForm.getRawValue() as any).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.toast.success('Your request has been submitted successfully.');
        this.requestForm.patchValue({ description: '' });
        this.loadRequests();
      },
      error: () => {
        this.isSubmitting = false;
        this.toast.error('Unable to submit the request. Please try again.');
      },
    });
  }

  updateStatus(id: number, status: RequestStatus): void {
    this.civic.updateStatus(id, status).subscribe({
      next: () => {
        this.toast.success('Request status updated.');
        this.loadRequests();
      },
      error: () => this.toast.error('Unable to update request status.'),
    });
  }

  loadRequests(): void {
    this.civic.getMyRequests().subscribe({
      next: data => { this.requests = data; this.loading = false; },
      error: () => { this.loading = false; this.toast.error('Could not load your requests.'); },
    });
  }

  get pendingCount(): number { return this.requests.filter(r => r.status === 'PENDING').length; }
  get assignedCount(): number { return this.requests.filter(r => !!r.mentorName).length; }

  badgeClass(status: RequestStatus): string {
    const map: Record<RequestStatus, string> = {
      PENDING: 'text-bg-warning',
      ACCEPTED: 'text-bg-primary',
      IN_PROGRESS: 'text-bg-info',
      COMPLETED: 'text-bg-success',
      CANCELLED: 'text-bg-secondary',
    };
    return map[status] ?? 'text-bg-secondary';
  }
}
