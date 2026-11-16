export type TripStatus = 'Upcoming' | 'Ongoing' | 'Completed';


export function todayAsText(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}

export function getTripStatus(startDate: string, endDate: string, today: string): TripStatus {
  if (today < startDate) {
    return 'Upcoming';
  }
  if (today > endDate) {
    return 'Completed';
  }
  return 'Ongoing';
}

// "2026-07-01" -> a Date at local midnight (new Date("2026-07-01") would use UTC
// and could show the wrong day).
function toDate(text: string): Date {
  const [year, month, day] = text.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function getDays(startDate: string, endDate: string): number {
  const millisecondsPerDay = 24 * 60 * 60 * 1000;
  // Math.round because a daylight saving change makes a day 23 or 25 hours long
  const difference = Math.round(
    (toDate(endDate).getTime() - toDate(startDate).getTime()) / millisecondsPerDay,
  );
  return difference + 1;
}

function plural(count: number, word: string): string {
  return `${count} ${word}${count === 1 ? '' : 's'}`;
}

// 1 July - 5 July is "5 days · 4 nights"
export function formatDuration(startDate: string, endDate: string): string {
  const days = getDays(startDate, endDate);
  return `${plural(days, 'day')} · ${plural(days - 1, 'night')}`;
}

// "Jul 1 – Jul 5, 2026" (the year is shown on both dates if the years differ)
export function formatDateRange(startDate: string, endDate: string): string {
  const start = toDate(startDate);
  const end = toDate(endDate);
  const short: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric' };
  const long: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };

  if (start.getFullYear() === end.getFullYear()) {
    return `${start.toLocaleDateString('en-US', short)} – ${end.toLocaleDateString('en-US', long)}`;
  }
  return `${start.toLocaleDateString('en-US', long)} – ${end.toLocaleDateString('en-US', long)}`;
}
