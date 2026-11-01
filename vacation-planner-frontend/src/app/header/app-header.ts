import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {SiteLogo} from './site-logo';
import {UserMenu} from './user-menu';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, SiteLogo, UserMenu],
  templateUrl: './app-header.html',
})
export class AppHeader {
}
