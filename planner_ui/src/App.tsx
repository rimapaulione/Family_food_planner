import {QueryClient, QueryClientProvider} from '@tanstack/react-query';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {staleTime: 30_000, retry: 1},
    },
})

function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <div className="bg-background text-foreground">
                <button className="bg-primary text-primary-foreground rounded-md">Save</button>
                <p className="text-muted-foreground">Description</p>
                <div className="bg-card text-card-foreground rounded-lg">Card</div>
            </div>
        </QueryClientProvider>
    );
}

export default App;
