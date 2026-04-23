import axios from 'axios';
import {useAuthStore} from '@/stores/useAuthStore';

const api = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use((config) => {
    const token = useAuthStore.getState().token;
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;
        const url: string = error.config?.url ?? '';
        const isAuthEndpoint = url.includes('/auth/');

        if (status === 401 && !isAuthEndpoint) {
            useAuthStore.getState().clearAuth();
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    },
);

export default api;
