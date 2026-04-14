import type {ReactNode} from 'react';

interface FormFieldProps {
    label: string;
    error?: string;
    children: ReactNode;
}

export function FormField({label, error, children}: FormFieldProps) {
    return (
        <div>
            <label className="mb-1 block text-sm font-medium">{label}</label>
            {children}
            <p className="mt-1 min-h-4 text-xs text-destructive">{error}</p>
        </div>
    );
}
