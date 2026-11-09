import {Component} from '@angular/core';
import {Drawer} from 'primeng/drawer';
import {UserMenu} from './user-menu';
import {MobileNavigation} from './mobile-navigation';

@Component({
  selector: 'mobile-drawer',
  imports: [
    Drawer,
    UserMenu,
    MobileNavigation
  ],
  templateUrl: './mobile-drawer.html',
})
export class MobileDrawer {
  visible = false
}
