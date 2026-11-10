import { Routes } from '@angular/router';
import { TestComponent } from './test-component/test-component';
import { Welcome } from './welcome/welcome';
import { Flights } from './pages/flights';
import { Trips } from './pages/trips';
import { Restaurants } from './pages/restaurants';
import { Accommodations } from './pages/accommodations';
import {Login} from './pages/login';

export const routes: Routes = [
  { path: 'test', component: TestComponent },
  { path: 'example', component: Welcome },
  { path: 'trips', component: Trips},
  { path: 'flights', component: Flights},
  { path: 'accommodations', component: Accommodations},
  { path: 'restaurants', component: Restaurants},
  { path: 'login', component: Login},
];
