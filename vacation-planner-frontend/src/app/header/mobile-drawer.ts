import {Component} from '@angular/core';
import {Drawer} from 'primeng/drawer';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {UserMenu} from './user-menu';

@Component({
  selector: 'mobile-drawer',
  imports: [
    Drawer,
    RouterLink,
    RouterLinkActive,
    UserMenu
  ],
  templateUrl: './mobile-drawer.html',
})
export class MobileDrawer {
  visible = false
}
