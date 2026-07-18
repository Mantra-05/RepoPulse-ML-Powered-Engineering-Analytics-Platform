export function SkeletonCard({ className = '' }: { className?: string }) {
  return <div className={`skeleton h-32 ${className}`} />;
}

export function SkeletonRow() {
  return (
    <div className="flex items-center gap-3 p-4">
      <div className="skeleton w-10 h-10 rounded-full" />
      <div className="flex-1 space-y-2">
        <div className="skeleton h-3 w-2/3 rounded" />
        <div className="skeleton h-3 w-1/3 rounded" />
      </div>
    </div>
  );
}

export function SkeletonTable({ rows = 5 }: { rows?: number }) {
  return (
    <div className="card divide-y divide-surface-border">
      {Array.from({ length: rows }).map((_, i) => (
        <SkeletonRow key={i} />
      ))}
    </div>
  );
}
