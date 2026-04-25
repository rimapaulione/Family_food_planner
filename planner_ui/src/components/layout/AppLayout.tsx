import { Outlet } from 'react-router-dom';
import { Header } from './Header';

export function AppLayout() {
  return (
    <div className="min-h-dvh bg-background overflow-x-hidden">
      <Header />
      <main className="mx-auto max-w-5xl px-4 md:px-6 lg:px-8 py-6">
        <Outlet />
      </main>
    </div>
  );
}
