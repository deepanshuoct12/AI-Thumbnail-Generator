import { Routes } from '@angular/router';
import { UploadComponent } from './features/upload/upload.component';

export const routes: Routes = [
  { path: '', component: UploadComponent },
  {
    path: 'results/:id',
    loadComponent: () => import('./features/results/results.component').then(m => m.ResultsComponent)
  },
  { path: '**', redirectTo: '' }
];
