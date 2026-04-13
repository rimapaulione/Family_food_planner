import {QueryCache, QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {BrowserRouter, Routes, Route, Navigate} from 'react-router-dom';
import {toast} from 'sonner';
import {AppLayout} from '@/components/layout/AppLayout';
import {RecipesPage} from '@/pages/RecipesPage';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {staleTime: 30_000, retry: 1},
    },
    queryCache: new QueryCache({
        onError: (error) => {
            const message = error instanceof Error ? error.message : 'Something went wrong';
            toast.error(message, {id: message});
        },
    }),
});

function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <BrowserRouter>
                <Routes>
                    <Route element={<AppLayout/>}>
                        <Route path="/" element={<div>Home</div>}/>
                        <Route path="/recipes" element={<RecipesPage/>}/>
                        <Route path="/planner" element={<div>Planner</div>}/>
                        <Route path="/shopping" element={<div>Shopping</div>}/>
                        <Route path="/basics" element={<div>Always Buy</div>}/>
                        <Route path="*" element={<Navigate to="/" replace/>}/>
                    </Route>

                </Routes>
            </BrowserRouter>
        </QueryClientProvider>
    );
}

export default App;
