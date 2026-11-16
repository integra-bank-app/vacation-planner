import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { Card } from 'primeng/card';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Skeleton } from 'primeng/skeleton';
import { Tag } from 'primeng/tag';
import { ApiConfiguration } from '../api/api-configuration';
import { PageResponseTripResponse } from '../api/models/page-response-trip-response';
import { TripResponse } from '../api/models/trip-response';
import { CurrentUser } from '../current-user';
import {
  TripStatus,
  formatDateRange,
  formatDuration,
  getTripStatus,
  todayAsText,
} from './trip-utils';

export type StatusFilter = 'All statuses' | TripStatus;

// What one card needs to show
interface TripCard {
  id: string;
  destination: string;
  country: string;
  status: TripStatus;
  dateRange: string;
  duration: string;
}

@Component({
  selector: 'trips',
  imports: [FormsModule, ButtonModule, Card, IconField, InputIcon, InputText, Select, Skeleton, Tag],
  templateUrl: './trips.html',
})
export class Trips implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApiConfiguration);
  private readonly router = inject(Router);
  private readonly currentUser = inject(CurrentUser);

  // state of the request
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly trips = signal<TripResponse[]>([]);

  // state of the filters
  readonly search = signal('');
  readonly statusFilter = signal<StatusFilter>('All statuses');
  readonly statusOptions: StatusFilter[] = ['All statuses', 'Upcoming', 'Ongoing', 'Completed'];

  // 6 grey placeholder cards while loading
  readonly skeletons = [1, 2, 3, 4, 5, 6];

  // every trip turned into a card (computed again only when trips changes)
  private readonly allCards = computed<TripCard[]>(() => {
    const today = todayAsText();
    return this.trips().map((trip) => {
      const start = trip.startDate ?? '';
      const end = trip.endDate ?? '';
      const [city, country] = splitDestination(trip.destination ?? '');
      return {
        id: trip.id ?? '',
        destination: city,
        country,
        status: getTripStatus(start, end, today),
        dateRange: formatDateRange(start, end),
        duration: formatDuration(start, end),
      };
    });
  });

  // the cards left after the search and the status filter
  readonly cards = computed(() => {
    const text = this.search().trim().toLowerCase();
    const status = this.statusFilter();
    return this.allCards().filter(
      (card) =>
        card.destination.toLowerCase().includes(text) &&
        (status === 'All statuses' || card.status === status),
    );
  });

  readonly resultLabel = computed(() => {
    if (this.loading()) {
      return 'Loading your trips…';
    }
    const count = this.cards().length;
    return `${count} ${count === 1 ? 'trip' : 'trips'} · Updated today`;
  });

  ngOnInit(): void {
    this.loadTrips();
  }

  loadTrips(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    // The backend wants a date range, so we ask for "all dates".
    // size=100 is the biggest page the backend allows.
    const params = new HttpParams()
      .set('username', this.currentUser.username)
      .set('startDate', '1900-01-01')
      .set('endDate', '2999-12-31')
      .set('page', 0)
      .set('size', 100);

    this.http.get<PageResponseTripResponse>(`${this.config.rootUrl}/trips`, { params }).subscribe({
      next: (response) => {
        this.trips.set(response.content ?? []);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(
          error.status === 404
            ? 'We could not find your account, so there are no trips to show.'
            : 'Something went wrong while loading your trips. Please try again.',
        );
        this.loading.set(false);
      },
    });
  }

  viewTrip(id: string): void {
    this.router.navigate(['/trips', id]);
  }
}

// The backend only has "destination", so "Paris, France" is split into city and country.
// If there is no comma, the country stays empty.
function splitDestination(destination: string): [string, string] {
  const parts = destination.split(',');
  return [parts[0].trim(), parts.slice(1).join(',').trim()];
}
