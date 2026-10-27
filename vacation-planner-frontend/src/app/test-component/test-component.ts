import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputText } from 'primeng/inputtext';

@Component({
  selector: 'app-test-component',
  imports: [FormsModule, Button, Card, InputText],
  templateUrl: './test-component.html',
  styleUrl: './test-component.css',
})
export class TestComponent {
  protected readonly destination = signal('');
}
