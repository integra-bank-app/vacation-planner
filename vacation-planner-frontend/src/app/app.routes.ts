import { Routes } from '@angular/router';
import { TestComponent } from './test-component/test-component';
import { Welcome } from './welcome/welcome';

export const routes: Routes = [
  { path: 'test', component: TestComponent },
  { path: 'example', component: Welcome },
];
