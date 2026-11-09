import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';

@Component({
  selector: 'mobile-navigation',
  imports: [
    RouterLink,
    RouterLinkActive,
  ],
  template: `<nav>
  <a routerLink="/trips" class="hover:underline" routerLinkActive="text-primary">My trips</a><br>
  <a routerLink="/flights" class="hover:underline" routerLinkActive="text-primary">Flights</a><br>
  <a routerLink="/accommodations" class="hover:underline" routerLinkActive="text-primary">Accommodations</a><br>
  <a routerLink="/restaurants" class="hover:underline" routerLinkActive="text-primary">Restaurants</a>
  </nav>`
})
export class MobileNavigation {

}
