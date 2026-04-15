/**
 * Extracts a user-facing error message from an axios error response.
 * Falls back to the provided default if the backend didn't send a message.
 */
export const getErrorMessage = (err: unknown, fallback: string): string => {
    if (err && typeof err === 'object' && 'response' in err) {
        const response = (err as {response?: {data?: {message?: string}}}).response;
        if (response?.data?.message) return response.data.message;
    }
    return fallback;
};
