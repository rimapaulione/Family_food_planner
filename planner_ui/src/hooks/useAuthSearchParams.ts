import {useSearchParams} from 'react-router-dom';

export function useAuthSearchParams() {
    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get('redirectTo') ?? '/';
    const email = searchParams.get('email') ?? undefined;
    const params = searchParams.toString();
    const siblingHref = (path: string) => params ? `${path}?${params}` : path;
    return {redirectTo, email, siblingHref};
}
