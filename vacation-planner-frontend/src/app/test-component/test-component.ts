import { CreateTripDialog } from '../create-trip-dialog/create-trip-dialog';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputText } from 'primeng/inputtext';

@Component({
  selector: 'app-test-component',
  imports: [FormsModule, Button, Card, InputText, CreateTripDialog],
  templateUrl: './test-component.html',
  styleUrl: './test-component.css',
})
export class TestComponent {
  protected readonly destination = signal('');

  protected readonly createTripOpen = signal(false);

  protected openCreateTrip(): void {
    this.createTripOpen.set(true);
  }

  protected onTripCreated(id: string): void {
    console.log('Trip created, the trips list would reload now. New id:', id);
  }
}
