interface NotFoundProps {
    message?: string;
}

export function NotFound({message = 'Not found'}: NotFoundProps) {
    return <p className="text-muted-foreground">{message}</p>;
}
