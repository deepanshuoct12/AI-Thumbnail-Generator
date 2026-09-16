import { Component, Input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loader',
  standalone: true,
  imports: [MatProgressSpinnerModule],
  template: `
    <div class="loader">
      <mat-spinner diameter="40"></mat-spinner>
      <span>{{ message }}</span>
    </div>
  `,
  styles: [`
    .loader { display: flex; align-items: center; gap: 1rem; margin: 1rem 0; }
  `]
})
export class LoaderComponent {
  @Input() message = 'Loading...';
}
