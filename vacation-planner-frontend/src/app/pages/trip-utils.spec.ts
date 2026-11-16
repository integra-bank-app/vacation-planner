import { formatDateRange, formatDuration, getTripStatus } from './trip-utils';

describe('trip-utils', () => {
  describe('getTripStatus', () => {
    it('is Upcoming before the start date', () => {
      expect(getTripStatus('2026-07-01', '2026-07-07', '2026-06-30')).toBe('Upcoming');
    });

    it('is Ongoing on the first and on the last day', () => {
      expect(getTripStatus('2026-07-01', '2026-07-07', '2026-07-01')).toBe('Ongoing');
      expect(getTripStatus('2026-07-01', '2026-07-07', '2026-07-07')).toBe('Ongoing');
    });

    it('is Completed after the end date', () => {
      expect(getTripStatus('2026-07-01', '2026-07-07', '2026-07-08')).toBe('Completed');
    });
  });

  describe('formatDuration', () => {
    it('shows days and nights', () => {
      expect(formatDuration('2026-07-01', '2026-07-05')).toBe('5 days · 4 nights');
    });

    it('uses the singular for a one day trip', () => {
      expect(formatDuration('2026-07-01', '2026-07-01')).toBe('1 day · 0 nights');
    });

    it('is correct when the trip crosses a month change', () => {
      expect(formatDuration('2026-01-30', '2026-02-02')).toBe('4 days · 3 nights');
    });
  });

  describe('formatDateRange', () => {
    it('shows the year once when both dates are in the same year', () => {
      expect(formatDateRange('2026-07-01', '2026-07-07')).toBe('Jul 1 – Jul 7, 2026');
    });

    it('shows the year on both dates when the years differ', () => {
      expect(formatDateRange('2026-12-30', '2027-01-02')).toBe('Dec 30, 2026 – Jan 2, 2027');
    });
  });
});
