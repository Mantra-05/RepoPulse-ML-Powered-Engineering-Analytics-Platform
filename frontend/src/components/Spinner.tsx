import { Loader2 } from 'lucide-react';

interface SpinnerProps { size?: number; className?: string; }

export function Spinner({ size = 20, className = '' }: SpinnerProps) {
  return <Loader2 size={size} className={`animate-spin text-brand-400 ${className}`} />;
}

export function FullPageSpinner() {
  return (
    <div className="fixed inset-0 flex items-center justify-center bg-surface/80 backdrop-blur-sm z-50">
      <div className="flex flex-col items-center gap-3">
        <Spinner size={36} />
        <p className="text-slate-400 text-sm">Loading…</p>
      </div>
    </div>
  );
}
