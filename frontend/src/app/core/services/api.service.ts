import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private backendUrl = environment.apiBackendUrl;
  private fastApiUrl = environment.apiFastApiUrl;

  constructor(private http: HttpClient) {}

  checkBackendHealth(): Observable<unknown> {
    return this.http.get(`${this.backendUrl}/api/health`);
  }

  checkFastApiHealth(): Observable<unknown> {
    return this.http.get(`${this.fastApiUrl}/api/v1/health`);
  }
}
