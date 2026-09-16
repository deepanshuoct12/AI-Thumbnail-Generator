import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-message',
  standalone: true,
  template: `<div class="error">{{ message }}</div>`,
  styles: [`
    .error { color: #b00020; margin: 1rem 0; }
  `]
})
export class ErrorMessageComponent {
  @Input() message = '';
}
