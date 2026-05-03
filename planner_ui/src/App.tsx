import {QueryCache, QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {BrowserRouter, Routes, Route, Navigate} from 'react-router-dom';
import {toast} from 'sonner';
import {AppLayout} from '@/components/layout/AppLayout';
import {FamilyGuard} from '@/components/layout/FamilyGuard';
import {ProtectedRoute} from '@/components/layout/ProtectedRoute';
import {PublicOnlyRoute} from '@/components/layout/PublicOnlyRoute';
import {HomePage} from '@/pages/HomePage';
import {LoginPage} from '@/pages/auth/LoginPage';
import {RegisterPage} from '@/pages/auth/RegisterPage';
import {NoFamilyPage} from '@/pages/family/NoFamilyPage';
import {FamilySetupPage} from '@/pages/family/FamilySetupPage';
import {RecipeListPage} from '@/pages/recipes/RecipeListPage';
import {RecipeDetailPage} from '@/pages/recipes/RecipeDetailPage';
import {RecipeNewPage} from '@/pages/recipes/RecipeNewPage';
import {RecipeEditPage} from '@/pages/recipes/RecipeEditPage';
import {ProfilePage} from '@/pages/ProfilePage';
import {FamilyPage} from '@/pages/family/FamilyPage';
import {JoinPage} from '@/pages/family/JoinPage';
import {PlannerPage} from '@/pages/planner/PlannerPage';

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
                    <Route path="/join/:token" element={<JoinPage/>}/>
                    <Route element={<PublicOnlyRoute/>}>
                        <Route path="/login" element={<LoginPage/>}/>
                        <Route path="/register" element={<RegisterPage/>}/>
                    </Route>
                    <Route element={<ProtectedRoute/>}>
                        <Route path="/family/no-family" element={<NoFamilyPage/>}/>
                        <Route path="/family/setup" element={<FamilySetupPage/>}/>
                        <Route element={<FamilyGuard/>}>
                            <Route element={<AppLayout/>}>
                                <Route path="/" element={<HomePage/>}/>
                                <Route path="/recipes">
                                    <Route index element={<RecipeListPage/>}/>
                                    <Route path="new" element={<RecipeNewPage/>}/>
                                    <Route path=":id" element={<RecipeDetailPage/>}/>
                                    <Route path=":id/edit" element={<RecipeEditPage/>}/>
                                </Route>
                                <Route path="/planner" element={<PlannerPage/>}/>
                                <Route path="/shopping" element={<div>Shopping</div>}/>
                                <Route path="/profile" element={<ProfilePage/>}/>
                                <Route path="/family" element={<FamilyPage/>}/>
                                <Route path="*" element={<Navigate to="/" replace/>}/>
                            </Route>
                        </Route>
                    </Route>

                </Routes>
            </BrowserRouter>
        </QueryClientProvider>
    );
}

export default App;
