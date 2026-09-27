import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AuthService, UserResponse } from '../../services/auth.service';
import { AdminStats, CivicService, Mentor } from '../../services/civic.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css',
})
export class AdminDashboardComponent implements OnInit {
  user: UserResponse | null = null;
  stats: AdminStats = {
    users: 0,
    mentors: 0,
    pendingMentors: 0,
    requests: 0,
    pendingRequests: 0,
  };
  pendingMentors: Mentor[] = [];
  loading = true;

  message = '';
  error = '';
  constructor(
    private auth: AuthService,
    private civic: CivicService,
    private toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.auth.getCurrentUser().subscribe({ next: (u) => (this.user = u) });
    this.load();
  }

  load(): void {
    this.loading = true;
    this.civic.getAdminStats().subscribe({
      next: (s) => (this.stats = s),
      error: () => this.toast.error('Could not load platform statistics.'),
    });
    this.civic.getPendingMentors().subscribe({
      next: (m) => {
        this.pendingMentors = m;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.toast.error('Could not load pending mentors.');
      },
    });
  }

  verify(mentor: Mentor, status: 'APPROVED' | 'REJECTED'): void {
    this.civic.verifyMentor(mentor.id, status).subscribe({
      next: () => {
        this.toast.success(
          `Mentor ${status === 'APPROVED' ? 'approved' : 'rejected'} successfully.`,
        );
        this.load();
      },
      error: () => this.toast.error('Unable to update mentor verification.'),
    });
  }
}
