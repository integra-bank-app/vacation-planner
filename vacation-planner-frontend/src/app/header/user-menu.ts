import {Component} from '@angular/core';
import {Menu} from 'primeng/menu';
import {ButtonModule} from 'primeng/button';

@Component({
  selector: 'user-menu',
  imports: [Menu, ButtonModule],
  templateUrl: './user-menu.html',
})
export class UserMenu {
  options = [
    {
      separator: true
    },
    {
      label: "Logout",
      routerLink: "/login"
    }
  ]
}
