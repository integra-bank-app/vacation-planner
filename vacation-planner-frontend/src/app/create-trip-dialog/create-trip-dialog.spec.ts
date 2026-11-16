import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, provideRouter } from '@angular/router';
import { Api } from '../api/api';
import { createTrip } from '../api/fn/trip-controller/create-trip';
import { CreateTripDialog } from './create-trip-dialog';

const START = new Date(2099, 0, 10); // Jan 10, 2099 (months start at 0)
const END = new Date(2099, 0, 14);

describe('CreateTripDialog', () => {
  let fixture: ComponentFixture<CreateTripDialog>;
  let component: CreateTripDialog;
  let invoke: ReturnType<typeof vi.fn>;
  let router: Router;

  beforeEach(async () => {
    invoke = vi.fn();
    await TestBed.configureTestingModule({
      imports: [CreateTripDialog],
      providers: [provideRouter([]), { provide: Api, useValue: { invoke } }],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateTripDialog);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    component.visible.set(true);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  function fillValidDetails(destination = 'Lisbon, Portugal'): void {
    component['destination'].set(destination);
    component['dates'].set([START, END]);
  }

  function serverReturnsId(id: string): void {
    // The generated client gives a Blob; the dialog only calls .text() on it.
    invoke.mockResolvedValue({ text: () => Promise.resolve(JSON.stringify(id)) });
  }

  // Step navigation

  it('starts on step 1', () => {
    expect(component['step']()).toBe(1);
  });

  it('moves from step 1 to step 2 when the details are valid', () => {
    fillValidDetails();

    component['next']();

    expect(component['step']()).toBe(2);
  });

  it('moves from step 2 back to step 1 and keeps the details', () => {
    fillValidDetails();
    component['next']();

    component['back']();

    expect(component['step']()).toBe(1);
    expect(component['destination']()).toBe('Lisbon, Portugal');
    expect(component['dates']()).toEqual([START, END]);
  });

  // Validation blocking step 1 -> 2

  it.each([
    { name: 'the destination is empty', destination: '', dates: [START, END] },
    { name: 'the destination is only spaces', destination: '   ', dates: [START, END] },
    { name: 'there are no dates', destination: 'Lisbon', dates: null },
    { name: 'only the start date is picked', destination: 'Lisbon', dates: [START, null] },
    { name: 'the end date is the start date', destination: 'Lisbon', dates: [START, START] },
    { name: 'the trip starts in the past', destination: 'Lisbon', dates: [new Date(2000, 0, 1), END] },
  ])('stays on step 1 when $name', ({ destination, dates }) => {
    component['destination'].set(destination);
    component['dates'].set(dates);

    component['next']();

    expect(component['step']()).toBe(1);
  });

  // The POST /trips call

  it('sends POST /trips with the username, trimmed destination and dates', async () => {
    serverReturnsId('trip-123');
    fillValidDetails('  Lisbon, Portugal  ');

    await component['submitTrip']();

    expect(invoke).toHaveBeenCalledWith(createTrip, {
      body: {
        username: 'ana.pop',
        destination: 'Lisbon, Portugal',
        startDate: '2099-01-10',
        endDate: '2099-01-14',
      },
    });
  });

  it('moves to step 3 and emits the new trip id after saving', async () => {
    serverReturnsId('trip-123');
    fillValidDetails();
    component['step'].set(2);
    const emitted: string[] = [];
    component.tripCreated.subscribe(id => emitted.push(id));

    await component['submitTrip']();

    expect(component['step']()).toBe(3);
    expect(emitted).toEqual(['trip-123']);
  });

  it('stays on step 2 and keeps the entered data when saving fails', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    invoke.mockRejectedValue(new HttpErrorResponse({ status: 0 }));
    fillValidDetails();
    component['step'].set(2);

    await component['submitTrip']();

    expect(component['step']()).toBe(2);
    expect(component['saveError']()).toContain('could not reach the server');
    expect(component['destination']()).toBe('Lisbon, Portugal');
    expect(component['dates']()).toEqual([START, END]);
  });

  // Closing and navigating after step 3

  it('closes the dialog and navigates to the new trip on "View trip"', async () => {
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    serverReturnsId('trip-123');
    fillValidDetails();
    await component['submitTrip']();

    component['viewTrip']();

    expect(component.visible()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/trips', 'trip-123']);
  });

  it('resets to an empty step 1 when the dialog is closed', async () => {
    serverReturnsId('trip-123');
    fillValidDetails();
    await component['submitTrip']();

    component['reset']();

    expect(component['step']()).toBe(1);
    expect(component['destination']()).toBe('');
    expect(component['dates']()).toBeNull();
  });
});
