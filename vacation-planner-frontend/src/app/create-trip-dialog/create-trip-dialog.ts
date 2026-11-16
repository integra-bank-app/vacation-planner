import { Component, computed, inject, model, signal, output} from '@angular/core';
import { Dialog } from 'primeng/dialog';
import { Step, StepList, Stepper } from 'primeng/stepper';
import { Button } from 'primeng/button';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { DatePicker } from 'primeng/datepicker';
import { Message } from 'primeng/message';
import { Tag } from 'primeng/tag';
import { Api } from '../api/api';
import { createTrip } from '../api/fn/trip-controller/create-trip';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

// TODO: replace with the signed-in user once login exists.
const CURRENT_USERNAME = 'ana.pop';

function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}


function today(): Date {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  return date;
}

@Component({
  selector: 'app-create-trip-dialog',
  imports: [Dialog, Stepper, StepList, Step, Button, FormsModule, InputText, IconField, InputIcon, DatePicker, Message, Tag],
  templateUrl: './create-trip-dialog.html',
  styleUrl: './create-trip-dialog.css',
})

export class CreateTripDialog {
  readonly visible = model(false);

  readonly tripCreated = output<string>();

  protected readonly step = signal(1);

  protected readonly steps = [
    { value: 1, label: 'Details' },
    { value: 2, label: 'Review' },
    { value: 3, label: 'Confirm' },
  ];

  protected readonly destination = signal('');

  protected readonly dates = signal<(Date | null)[] | null>(null);

  protected readonly minDate = today();

  protected readonly days = computed(() => {
    const range = this.dates();
    const start = range?.[0];
    const end = range?.[1];
    if (!start || !end) {
      return null;
    }
    const msPerDay = 24 * 60 * 60 * 1000;
    return Math.round((end.getTime() - start.getTime()) / msPerDay) + 1;
  });

  protected readonly destinationValid = computed(() => this.destination().trim() !== '');

  protected readonly datesValid = computed(() => {
    const [start, end] = this.dates() ?? [];
    return !!start && !!end
      && start.getTime() >= this.minDate.getTime()
      && end.getTime() > start.getTime();
  });

  protected readonly stepOneValid = computed(() => this.destinationValid() && this.datesValid());

  protected readonly title = computed(() => {
    switch (this.step()) {
      case 2:
        return 'Review your trip';
      case 3:
        return 'Your trip is ready!';
      default:
        return 'Plan a new trip';
    }
  });

  protected readonly dateRangeText = computed(() => {
    const [start, end] = this.dates() ?? [];
    if (!start || !end) {
      return '';
    }
    return new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
      .formatRange(start, end);
  });

  private readonly api = inject(Api);

  private readonly router = inject(Router);

  protected readonly saving = signal(false);
  protected readonly saveError = signal<string | null>(null);
  protected readonly createdTripId = signal<string | null>(null);

  protected async submitTrip(): Promise<void> {
    const [start, end] = this.dates() ?? [];
    if (this.saving() || !start || !end) {
      return;
    }
    this.saving.set(true);
    this.saveError.set(null);
    try {
      const response = await this.api.invoke(createTrip, {
        body: {
          username: CURRENT_USERNAME,
          destination: this.destination().trim(),
          startDate: toIsoDate(start),
          endDate: toIsoDate(end),
        },
      });
      const id = JSON.parse(await (response as unknown as Blob).text()) as string;
      this.createdTripId.set(id);
      this.tripCreated.emit(id);
      this.step.set(3);
    } catch (err) {
      this.saveError.set(this.errorMessage(err));
    } finally {
      this.saving.set(false);
    }
  }

  private errorMessage(err: unknown): string {
    console.error('Creating the trip failed', err);
    if (err instanceof HttpErrorResponse && err.status === 0) {
      return 'We could not reach the server. Check your connection and try again.';
    }
    return 'Something went wrong while creating your trip. Please try again.';
  }

  protected next(): void {
    if (this.step() === 1 && !this.stepOneValid()) {
      return;
    }
    this.step.update(current => current + 1);
  }

  protected back(): void {
    this.step.update(current => current - 1);
  }

  protected close(): void {
    this.visible.set(false);
  }

  protected viewTrip(): void {
    const id = this.createdTripId();
    this.close();
    if (id) {
      void this.router.navigate(['/trips', id]);
    }
  }

  protected reset(): void {
    this.step.set(1);
    this.destination.set('');
    this.dates.set(null);
    this.saveError.set(null);
    this.createdTripId.set(null);
  }
}
