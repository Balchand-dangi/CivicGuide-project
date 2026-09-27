import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type RequestStatus = 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface ServiceRequest {
  id: number;
  category: string;
  serviceName: string;
  description: string;
  status: RequestStatus;
  userName: string;
  userEmail: string;
  mentorName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateServiceRequest {
  category: string;
  serviceName: string;
  description: string;
}

export interface Mentor {
  id: number;
  name: string;
  email: string;
  qualification: string;
  specialization: string;
  experienceYears: number;
  languages: string;
  bio: string;
  verificationStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export interface AdminStats {
  users: number;
  mentors: number;
  pendingMentors: number;
  requests: number;
  pendingRequests: number;
}

@Injectable({ providedIn: 'root' })
export class CivicService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  createRequest(request: CreateServiceRequest): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(`${this.baseUrl}/requests`, request, { withCredentials: true });
  }

  getMyRequests(): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(`${this.baseUrl}/requests/mine`, { withCredentials: true });
  }

  getAvailableRequests(): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(`${this.baseUrl}/requests/available`, { withCredentials: true });
  }

  getMentorRequests(): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(`${this.baseUrl}/requests/mentor`, { withCredentials: true });
  }

  acceptRequest(id: number): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(`${this.baseUrl}/requests/${id}/accept`, {}, { withCredentials: true });
  }

  updateStatus(id: number, status: RequestStatus): Observable<ServiceRequest> {
    return this.http.patch<ServiceRequest>(`${this.baseUrl}/requests/${id}/status?status=${status}`, {}, { withCredentials: true });
  }

  getAdminStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.baseUrl}/admin/stats`, { withCredentials: true });
  }

  getPendingMentors(): Observable<Mentor[]> {
    return this.http.get<Mentor[]>(`${this.baseUrl}/admin/mentors/pending`, { withCredentials: true });
  }

  verifyMentor(id: number, status: 'APPROVED' | 'REJECTED'): Observable<Mentor> {
    return this.http.patch<Mentor>(`${this.baseUrl}/admin/mentors/${id}/verification?status=${status}`, {}, { withCredentials: true });
  }
}
