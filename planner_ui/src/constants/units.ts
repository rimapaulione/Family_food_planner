export const UNITS = ['g', 'ml', 'vnt'] as const;

export type Unit = typeof UNITS[number];
