import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Toast, ToastService } from '../../services/toast.service';

@Component({
    selector: 'app-toast',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="toast-wrapper" aria-live="polite" aria-atomic="true">
      @for (toast of toasts; track toast.id) {
        <div class="toast-item show toast-{{ toast.type }}" role="alert">
          <div class="d-flex align-items-center gap-2">
            <i class="bi {{ iconFor(toast.type) }}"></i>
            <span>{{ toast.message }}</span>
            <button type="button" class="toast-close ms-auto" (click)="dismiss(toast.id)" aria-label="Close">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
        </div>
      }
    </div>
  `,
    styles: [`
    .toast-wrapper {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: .65rem;
      min-width: 300px;
      max-width: 420px;
    }
    .toast-item {
      padding: .85rem 1rem;
      border-radius: 12px;
      font-size: .92rem;
      font-weight: 500;
      box-shadow: 0 8px 24px rgba(0,0,0,.14);
      animation: slideIn .28s ease;
      color: #fff;
    }
    .toast-success  { background: #18a058; }
    .toast-error    { background: #d03050; }
    .toast-info     { background: #2080f0; }
    .toast-warning  { background: #f0a020; }
    .toast-close {
      background: none;
      border: none;
      color: inherit;
      opacity: .75;
      cursor: pointer;
      padding: 0 .2rem;
      line-height: 1;
      flex-shrink: 0;
    }
    .toast-close:hover { opacity: 1; }
    @keyframes slideIn {
      from { transform: translateX(110%); opacity: 0; }
      to   { transform: translateX(0);   opacity: 1; }
    }
  `]
})
export class ToastComponent implements OnInit {
    toasts: Toast[] = [];

    constructor(private toastService: ToastService) { }

    ngOnInit(): void {
        this.toastService.toasts$.subscribe(toasts => this.toasts = toasts);
    }

    dismiss(id: number): void { this.toastService.dismiss(id); }

    iconFor(type: Toast['type']): string {
        return {
            success: 'bi-check-circle-fill',
            error: 'bi-x-circle-fill',
            info: 'bi-info-circle-fill',
            warning: 'bi-exclamation-triangle-fill',
        }[type];
    }
}
