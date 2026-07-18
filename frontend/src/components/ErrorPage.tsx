import { useNavigate } from 'react-router-dom';
import { AlertTriangle } from 'lucide-react';

interface ErrorPageProps { code?: number; message?: string; }

export function ErrorPage({ code = 500, message = 'Something went wrong' }: ErrorPageProps) {
  const navigate = useNavigate();
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center px-4 animate-fade-in">
      <AlertTriangle size={48} className="text-red-400 mb-4" />
      <h1 className="text-6xl font-bold text-gradient mb-2">{code}</h1>
      <p className="text-slate-400 text-lg mb-8">{message}</p>
      <button onClick={() => navigate(-1)} className="btn-primary">Go Back</button>
    </div>
  );
}

export function NotFoundPage() {
  return <ErrorPage code={404} message="Page not found" />;
}
