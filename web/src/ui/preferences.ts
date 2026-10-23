export type TableStyle = 'room' | 'simple';

const KEY = 'mallorca.tableStyle';

export function readTableStyle(): TableStyle {
  try {
    return localStorage.getItem(KEY) === 'simple' ? 'simple' : 'room';
  } catch {
    return 'room';
  }
}

export function writeTableStyle(style: TableStyle): void {
  try {
    localStorage.setItem(KEY, style);
  } catch {
    return;
  }
}
