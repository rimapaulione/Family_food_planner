export const inputClass = (hasError: boolean) =>
    `w-full rounded-md border ${hasError ? 'border-destructive' : 'border-input'} bg-background px-3 py-2 text-base md:text-sm outline-none focus:ring-2 focus:ring-ring`;
