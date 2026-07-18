import { Outlet, Navigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import { useAuth } from '../context/AuthContext';
import { FullPageSpinner } from './Spinner';

export default function Layout() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <FullPageSpinner />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-surface text-slate-100 flex">
      {/* Sidebar handles its own mobile responsiveness */}
      <Sidebar />
      
      {/* Main Content Area */}
      <main className="flex-1 lg:ml-64 min-w-0 transition-all duration-300">
        <div className="max-w-7xl mx-auto w-full pt-16 lg:pt-0">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
