import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { ConfigService } from '../../core/services/config.service';
import { VideoService } from '../../core/services/video.service';
import { AppConfig } from '../../core/models/config.model';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    LoaderComponent,
    ErrorMessageComponent
  ],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss'
})
export class UploadComponent implements OnInit {
  private fb = inject(FormBuilder);
  private configService = inject(ConfigService);
  private videoService = inject(VideoService);
  private router = inject(Router);

  uploadForm!: FormGroup;
  config?: AppConfig;
  loading = false;
  error?: string;

  ngOnInit() {
    this.uploadForm = this.fb.group({
      file: [null, Validators.required],
      style: ['', Validators.required],
      resolution: ['', Validators.required],
      count: [5, [Validators.required, Validators.min(1), Validators.max(5)]]
    });

    this.configService.getConfig().subscribe({
      next: c => this.config = c,
      error: () => this.error = 'Could not load config'
    });
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.uploadForm.patchValue({ file: input.files[0] });
    }
  }

  submit() {
    if (this.uploadForm.invalid || !this.uploadForm.value.file) return;

    this.loading = true;
    this.error = undefined;

    const formData = new FormData();
    formData.append('file', this.uploadForm.value.file);
    formData.append('style', this.uploadForm.value.style);
    formData.append('resolution', this.uploadForm.value.resolution);
    formData.append('count', String(this.uploadForm.value.count));

    this.videoService.upload(formData).subscribe({
      next: res => this.router.navigate(['/results', res.videoId]),
      error: err => {
        this.loading = false;
        this.error = err.error?.message || 'Upload failed';
      }
    });
  }
}
