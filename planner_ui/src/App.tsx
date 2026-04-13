import {QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {BrowserRouter, Routes, Route} from 'react-router-dom';
import {AppLayout} from '@/components/layout/AppLayout';
import {RecipesPage} from '@/pages/RecipesPage';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {staleTime: 30_000, retry: 1},
    },
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
                    </Route>
                </Routes>
            </BrowserRouter>
        </QueryClientProvider>
    );
}

export default App;
