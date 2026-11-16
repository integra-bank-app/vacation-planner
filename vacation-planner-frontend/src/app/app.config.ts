import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

import { routes } from './app.routes';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import { authInterceptor } from './auth/auth.interceptor';

const VacationPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{teal.50}',
      100: '#b3ecec',
      200: '#89ecda',
      300: '#43e8d8',
      400: '#40e0d0',
      500: '#3bd6c6',
      600: '{teal.600}',
      700: '{teal.700}',
      800: '{teal.800}',
      900: '{teal.900}',
      950: '{teal.950}',
    },
    colorScheme: {
      light: {
        primary: {
          color: '#40e0d0',
          contrastColor: '#0f172a',
          hoverColor: '#3bd6c6',
          activeColor: '{teal.500}',
        },
      },
    },
  },
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    providePrimeNG({
      theme: {
        preset: VacationPreset,
        options: {
          darkModeSelector: false,
        },
      },
    })

  ]
};
