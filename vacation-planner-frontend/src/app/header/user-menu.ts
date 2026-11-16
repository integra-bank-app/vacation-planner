import {Component, inject} from '@angular/core';
import {Menu} from 'primeng/menu';
import {ButtonModule} from 'primeng/button';
import {AuthService} from '../auth/auth.service';

@Component({
  selector: 'user-menu',
  imports: [Menu, ButtonModule],
  templateUrl: './user-menu.html',
})
export class UserMenu {
  private readonly auth = inject(AuthService);

  options = [
    {
      separator: true
    },
    {
      label: "Logout",
      routerLink: "/login",
      command: () => this.auth.logout()
    }
  ]
}
