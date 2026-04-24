import {QueryCache, QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {BrowserRouter, Routes, Route, Navigate} from 'react-router-dom';
import {toast} from 'sonner';
import {AppLayout} from '@/components/layout/AppLayout';
import {ProtectedRoute} from '@/components/layout/ProtectedRoute';
import {PublicOnlyRoute} from '@/components/layout/PublicOnlyRoute';
import {HomePage} from '@/pages/HomePage';
import {LoginPage} from '@/pages/auth/LoginPage';
import {RegisterPage} from '@/pages/auth/RegisterPage';
import {RecipeListPage} from '@/pages/recipes/RecipeListPage';
import {RecipeDetailPage} from '@/pages/recipes/RecipeDetailPage';
import {RecipeNewPage} from '@/pages/recipes/RecipeNewPage';
import {RecipeEditPage} from '@/pages/recipes/RecipeEditPage';

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
                    <Route element={<PublicOnlyRoute/>}>
                        <Route path="/login" element={<LoginPage/>}/>
                        <Route path="/register" element={<RegisterPage/>}/>
                    </Route>
                    <Route element={<ProtectedRoute/>}>
                        <Route element={<AppLayout/>}>
                            <Route path="/" element={<HomePage/>}/>
                            <Route path="/recipes">
                                <Route index element={<RecipeListPage/>}/>
                                <Route path="new" element={<RecipeNewPage/>}/>
                                <Route path=":id" element={<RecipeDetailPage/>}/>
                                <Route path=":id/edit" element={<RecipeEditPage/>}/>
                            </Route>
                            <Route path="/planner" element={<div>Planner</div>}/>
                            <Route path="/shopping" element={<div>Shopping</div>}/>
                            <Route path="/basics" element={<div>Always Buy</div>}/>
                            <Route path="/profile" element={<div>User Profile</div>}/>
                            <Route path="*" element={<Navigate to="/" replace/>}/>
                        </Route>
                    </Route>

                </Routes>
            </BrowserRouter>
        </QueryClientProvider>
    );
}

export default App;
