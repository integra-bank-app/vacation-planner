import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {SiteLogo} from './site-logo';
import {UserMenu} from './user-menu';
import {MobileDrawer} from './mobile-drawer';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, SiteLogo, UserMenu, MobileDrawer],
  templateUrl: './app-header.html',
  host: {
    '(window:resize)': 'onResize($event)'
  }
})
export class AppHeader {
  windowWidth = window.innerWidth

  ngOnInit() {
    this.windowWidth = window.innerWidth
  }

  onResize(event: UIEvent) {
    const window = event.target as Window
    this.windowWidth = window.innerWidth
  }

}
