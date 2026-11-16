import { Injectable } from '@angular/core';

// There is no login yet, so every page uses this fixed user.
// TODO: when login works, return the logged-in user here.
@Injectable({ providedIn: 'root' })
export class CurrentUser {
  readonly username = 'ana.pop';
}
