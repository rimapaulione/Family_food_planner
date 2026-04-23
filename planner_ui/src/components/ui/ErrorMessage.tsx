interface ErrorMessageProps {
    error: unknown;
    fallback?: string;
}

export function ErrorMessage({error, fallback = 'Something went wrong'}: ErrorMessageProps) {
    const message = error instanceof Error ? error.message : fallback;
    return <p className="text-destructive">{message}</p>;
}
