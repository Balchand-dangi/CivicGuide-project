import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
    id: number;
    type: 'success' | 'error' | 'info' | 'warning';
    message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
    private counter = 0;
    private _toasts = new BehaviorSubject<Toast[]>([]);
    readonly toasts$ = this._toasts.asObservable();

    success(message: string): void { this.add('success', message); }
    error(message: string): void { this.add('error', message); }
    info(message: string): void { this.add('info', message); }
    warning(message: string): void { this.add('warning', message); }

    dismiss(id: number): void {
        this._toasts.next(this._toasts.value.filter(t => t.id !== id));
    }

    private add(type: Toast['type'], message: string): void {
        const id = ++this.counter;
        this._toasts.next([...this._toasts.value, { id, type, message }]);
        setTimeout(() => this.dismiss(id), 4500);
    }
}
