import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppHeader } from './app-header';
import {RouterModule} from '@angular/router';
import {routes} from '../app.routes'
import {RouterTestingHarness} from '@angular/router/testing';

describe('AppHeader', () => {
  let component: AppHeader;
  let fixture: ComponentFixture<AppHeader>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppHeader, RouterModule.forRoot(routes)],
    }).compileComponents();

    fixture = TestBed.createComponent(AppHeader);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should highlight active route', async () => {
    const compiled = fixture.nativeElement as HTMLElement;
    let navigationElements = compiled.querySelector("nav")?.children;
    expect(navigationElements).toBeTruthy();

    const routerHarness = await RouterTestingHarness.create();
    for (let i = 0; i < navigationElements?.length!; i++) {
      await routerHarness.navigateByUrl("/");

      for (let j = 0; j < navigationElements?.length!; j++) {
        let element = navigationElements![j];
        expect(element.classList).not.toContain("text-primary")
      }

      let highlightedElement = navigationElements![i];
      let route = highlightedElement.getAttribute("routerlink");
      expect(route).toBeTruthy();
      await routerHarness.navigateByUrl(route!);
      expect(highlightedElement.classList).toContain("text-primary");

      for (let j = 0; j < navigationElements?.length!; j++) {
        if (i == j) continue;
        let element = navigationElements![j];
        expect(element.classList).not.toContain("text-primary")
      }
    }
  });
});
