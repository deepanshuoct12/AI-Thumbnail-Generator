import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Video } from '../models/video.model';
import { environment } from '../environment/environment';

@Injectable({ providedIn: 'root' })
export class VideoService {
  constructor(private http: HttpClient) {}

  upload(formData: FormData): Observable<{ videoId: string; status: string }> {
    return this.http.post<{ videoId: string; status: string }>(`${environment.apiUrl}/videos/upload`, formData);
  }

  getVideo(id: string): Observable<Video> {
    return this.http.get<Video>(`${environment.apiUrl}/videos/${id}`);
  }

  downloadUrl(videoId: string, frameId: string): string {
    return `${environment.apiUrl}/videos/${videoId}/download?frameId=${frameId}`;
  }
}
