import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { Trips } from './trips';
import { TripResponse } from '../api/models/trip-response';

// Gives a date like "2026-11-20", some days from today (negative = in the past)
function daysFromToday(days: number): string {
  const date = new Date();
  date.setDate(date.getDate() + days);
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

const upcomingTrip: TripResponse = {
  id: '1',
  username: 'ana.pop',
  destination: 'Paris, France',
  startDate: daysFromToday(10),
  endDate: daysFromToday(14),
};
const ongoingTrip: TripResponse = {
  id: '2',
  username: 'ana.pop',
  destination: 'Rome, Italy',
  startDate: daysFromToday(-1),
  endDate: daysFromToday(2),
};
const completedTrip: TripResponse = {
  id: '3',
  username: 'ana.pop',
  destination: 'Vienna',
  startDate: daysFromToday(-20),
  endDate: daysFromToday(-15),
};

describe('Trips page', () => {
  let fixture: ComponentFixture<Trips>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Trips],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Trips);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges(); // starts the request
  });

  afterEach(() => http.verify());

  const page = () => fixture.nativeElement as HTMLElement;
  const text = () => page().textContent ?? '';
  const cardTitles = () =>
    Array.from(page().querySelectorAll('.trip-card .destination')).map((e) => e.textContent?.trim());

  // answers the GET /trips request with the given trips
  async function respondWith(trips: TripResponse[]) {
    const request = http.expectOne((r) => r.url.endsWith('/trips'));
    request.flush({ content: trips, page: 0, size: 100, totalElements: trips.length, totalPages: 1 });
    await fixture.whenStable();
  }

  async function type(searchText: string) {
    const input = page().querySelector('input') as HTMLInputElement;
    input.value = searchText;
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  it('asks for the trips of the current user', () => {
    const request = http.expectOne((r) => r.url.endsWith('/trips'));
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('username')).toBe('ana.pop');
    request.flush({ content: [] });
  });

  describe('loading state', () => {
    it('shows skeleton cards and the loading label', () => {
      expect(page().querySelectorAll('.skeleton-card').length).toBeGreaterThan(0);
      expect(page().querySelectorAll('.trip-card').length).toBe(0);
      expect(text()).toContain('Loading your trips…');

      http.expectOne((r) => r.url.endsWith('/trips')).flush({ content: [] });
    });
  });

  describe('empty state', () => {
    beforeEach(async () => respondWith([]));

    it('shows the empty message instead of the toolbar and the grid', () => {
      expect(text()).toContain('Your next adventure is waiting');
      expect(text()).toContain('Create your first trip');
      expect(text()).not.toContain('0 trips');
      expect(page().querySelector('input')).toBeNull();
      expect(page().querySelectorAll('.skeleton-card').length).toBe(0);
    });
  });

  describe('error state', () => {
    it('shows a message when the user is unknown (404)', async () => {
      http
        .expectOne((r) => r.url.endsWith('/trips'))
        .flush('User not found', { status: 404, statusText: 'Not Found' });
      await fixture.whenStable();

      expect(page().querySelector('[role="alert"]')).not.toBeNull();
      expect(text()).toContain('could not find your account');
      expect(text()).not.toContain('Loading your trips…');
    });

    it('shows a general message for other errors', async () => {
      http
        .expectOne((r) => r.url.endsWith('/trips'))
        .flush('boom', { status: 500, statusText: 'Server Error' });
      await fixture.whenStable();

      expect(text()).toContain('Something went wrong');
    });
  });

  describe('populated state', () => {
    beforeEach(async () => respondWith([upcomingTrip, ongoingTrip, completedTrip]));

    it('renders one card per trip', () => {
      expect(page().querySelectorAll('.trip-card').length).toBe(3);
      expect(page().querySelectorAll('.skeleton-card').length).toBe(0);
      expect(text()).toContain('3 trips · Updated today');
    });

    it('shows destination, country, status, dates and duration on a card', () => {
      const card = page().querySelector('.trip-card') as HTMLElement;

      expect(card.querySelector('.destination')?.textContent).toContain('Paris');
      expect(card.querySelector('.country')?.textContent).toContain('France');
      expect(card.querySelector('.status-tag')?.textContent).toContain('Upcoming');
      expect(card.querySelector('.date-range')?.textContent).toMatch(/\d{4}/);
      expect(card.querySelector('.duration')?.textContent).toContain('5 days · 4 nights');
      expect(card.querySelector('.totals')?.textContent).toContain('€0');
    });

    it('computes the status from the dates', () => {
      const tags = Array.from(page().querySelectorAll('.status-tag')).map((t) =>
        t.textContent?.trim(),
      );
      expect(tags).toEqual(['Upcoming', 'Ongoing', 'Completed']);
    });

    it('shows the disclaimer under the grid', () => {
      expect(text()).toContain(
        'Estimates include the flights, stays and restaurant visits added to each trip. Nothing is booked automatically.',
      );
    });

    it('opens /trips/:id when "View trip" is clicked', async () => {
      const router = TestBed.inject(Router);
      const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);

      const button = page().querySelector('.trip-card .view-trip button') as HTMLButtonElement;
      button.click();

      expect(navigate).toHaveBeenCalledWith(['/trips', '1']);
    });

    it('filters by destination, ignoring upper/lower case', async () => {
      await type('RO');

      expect(cardTitles()).toEqual(['Rome']);
      expect(text()).toContain('1 trip · Updated today');
    });

    it('filters by status', async () => {
      fixture.componentInstance.statusFilter.set('Completed');
      await fixture.whenStable();

      expect(cardTitles()).toEqual(['Vienna']);
      expect(text()).toContain('1 trip · Updated today');
    });

    it('combines the search and the status filter', async () => {
      fixture.componentInstance.statusFilter.set('Upcoming');
      await type('vienna');

      expect(cardTitles()).toEqual([]);
      expect(text()).toContain('0 trips · Updated today');
      expect(text()).toContain('No trips match your search.');
    });
  });
});
