import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AuthService, UserResponse } from '../../services/auth.service';
import {
  CivicService,
  ServiceRequest,
  RequestStatus,
} from '../../services/civic.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-mentor-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mentor-dashboard.component.html',
  styleUrl: './mentor-dashboard.component.css',
})
export class MentorDashboardComponent implements OnInit {
  user: UserResponse | null = null;
  available: ServiceRequest[] = [];
  mine: ServiceRequest[] = [];
  loading = true;
  pendingApproval = false;

  constructor(
    private auth: AuthService,
    private civic: CivicService,
    private toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.auth.getCurrentUser().subscribe({ next: (u) => (this.user = u) });
    console.log('Mentor dashboard loaded', this.user);
    this.load();
  }

  load(): void {
    this.loading = true;
    this.civic.getAvailableRequests().subscribe({
      next: (d) => (this.available = d),
      error: (e) => {
        if (e.status === 403) {
          this.pendingApproval = true;
        } else {
          this.toast.error('Could not load available requests.');
        }
      },
    });
    this.civic.getMentorRequests().subscribe({
      next: (d) => {
        this.mine = d;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.toast.error('Could not load your requests.');
      },
    });
  }

  accept(id: number): void {
    this.civic.acceptRequest(id).subscribe({
      next: () => {
        this.toast.success('Request accepted. The citizen has been notified.');
        this.load();
      },
      error: (e) => {
        this.toast.error(
          e.status === 403
            ? 'Your account is not approved yet.'
            : 'Unable to accept this request.',
        );
      },
    });
  }

  updateStatus(id: number, status: RequestStatus): void {
    this.civic.updateStatus(id, status).subscribe({
      next: () => {
        this.toast.success('Status updated.');
        this.load();
      },
      error: () => this.toast.error('Unable to update request status.'),
    });
  }

  get activeCount(): number {
    return this.mine.filter(
      (r) => r.status === 'ACCEPTED' || r.status === 'IN_PROGRESS',
    ).length;
  }
  get completedCount(): number {
    return this.mine.filter((r) => r.status === 'COMPLETED').length;
  }

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
