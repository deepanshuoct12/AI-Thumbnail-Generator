import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subject, timer } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';
import { VideoService } from '../../core/services/video.service';
import { Video } from '../../core/models/video.model';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { environment } from '../../core/environment/environment';

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    LoaderComponent,
    ErrorMessageComponent
  ],
  templateUrl: './results.component.html',
  styleUrl: './results.component.scss'
})
export class ResultsComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private videoService = inject(VideoService);
  private cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();

  video?: Video;
  loading = true;
  error?: string;

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    console.log('Results: id =', id);
    if (!id) {
      this.error = 'No video ID';
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    timer(0, 2000)
      .pipe(
        switchMap(() => this.videoService.getVideo(id)),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: v => {
          this.video = v;
          this.loading = false;
          this.cdr.detectChanges();
          if (v.status === 'COMPLETED' || v.status === 'FAILED') {
            this.destroy$.next();
            this.destroy$.complete();
          }
        },
        error: err => {
          this.error = err.error?.message || 'Failed to load video';
          this.loading = false;
          this.cdr.detectChanges();
          this.destroy$.next();
          this.destroy$.complete();
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  frameUrl(frameId: string): string {
    if (!this.video) return '';
    return `${environment.apiUrl}/videos/${this.video.id}/frame/${frameId}`;
  }

  download(frameId: string) {
    if (!this.video) return;
    window.open(this.videoService.downloadUrl(this.video.id, frameId), '_blank');
  }
}
