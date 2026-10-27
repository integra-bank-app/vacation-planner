import { Component, signal } from '@angular/core';
import { AppHeader } from './header/app-header';
import {RouterOutlet} from '@angular/router';
import {AppFooter} from '../footer/app-footer';

@Component({
  selector: 'app-root',
  imports: [AppHeader, RouterOutlet, AppFooter],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('vacation-planner-frontend');
}
