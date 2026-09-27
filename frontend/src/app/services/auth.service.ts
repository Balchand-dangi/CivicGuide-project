import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

export interface RegisterRequest {
  fullName: string;
  email: string;
  phone: string;
  password: string;
  confirmPassword: string;
  role: 'USER' | 'MENTOR';

  qualification?: string;
  specialization?: string;
  experienceYears?: number;
  languages?: string;
  bio?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  role: string;
  name: string;
}

export interface UserResponse {
  name: string;
  email: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';
  private readonly userApiUrl = 'http://localhost:8080/api/user';

  /**
   * Shared reactive state for the authenticated user.
   * The navbar and any other component can subscribe to this instead of
   * making independent HTTP calls, so the UI stays in sync after login/logout.
   */
  private _currentUser$ = new BehaviorSubject<UserResponse | null>(null);
  readonly currentUser$ = this._currentUser$.asObservable();

  constructor(private http: HttpClient) { }

  register(request: RegisterRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/register`, request, {
      responseType: 'text',
      withCredentials: true,
    });
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request, {
      withCredentials: true,
    }).pipe(
      tap(() => {
        // After a successful login, refresh the shared user state so the
        // navbar immediately shows Dashboard + Logout instead of Login/Register.
        this.refreshCurrentUser();
      })
    );
  }

  logout(): Observable<string> {
    return this.http.post(`${this.apiUrl}/logout`, {}, {
      responseType: 'text',
      withCredentials: true,
    }).pipe(
      tap(() => this._currentUser$.next(null))
    );
  }

  /**
   * Calls the backend to fetch the current user and pushes to the shared subject.
   * Used by NavbarComponent on init and after login.
   */
  refreshCurrentUser(): void {
    this.http.get<UserResponse>(`${this.userApiUrl}/me`, {
      withCredentials: true,
    }).pipe(
      catchError(() => of(null))
    ).subscribe(user => this._currentUser$.next(user));
  }

  /**
   * One-shot observable for guards and components that need the current user.
   * Returns whatever the backend says; null means unauthenticated.
   */
  getCurrentUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.userApiUrl}/me`, {
      withCredentials: true,
    });
  }

  isAuthenticated(): Observable<boolean> {
    return this.getCurrentUser().pipe(
      map(() => true),
      catchError(() => of(false)),
    );
  }
}
