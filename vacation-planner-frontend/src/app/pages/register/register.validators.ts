import { ValidatorFn } from '@angular/forms';

export const USERNAME_PATTERN = /^[a-z0-9]{3,20}$/;

export const EMAIL_PATTERN =
  /^[A-Za-z0-9_+-]+(\.[A-Za-z0-9_+-]+)*@([A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?\.)+[A-Za-z]{2,}$/;

export const MAX_NAME_LENGTH = 50;

export const passwordsMatchValidator: ValidatorFn = (group) => {
  const password = group.get('password')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordsMismatch: true };
};
