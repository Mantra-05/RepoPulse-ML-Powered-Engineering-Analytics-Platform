import clsx from 'clsx';

interface RiskBadgeProps { level: 'LOW' | 'MEDIUM' | 'HIGH' | string; }

export default function RiskBadge({ level }: RiskBadgeProps) {
  const l = level?.toUpperCase();
  return (
    <span className={clsx('badge', {
      'badge-low':    l === 'LOW',
      'badge-medium': l === 'MEDIUM',
      'badge-high':   l === 'HIGH',
    })}>
      <span className={clsx('w-1.5 h-1.5 rounded-full', {
        'bg-risk-low':    l === 'LOW',
        'bg-risk-medium': l === 'MEDIUM',
        'bg-risk-high':   l === 'HIGH',
      })} />
      {level}
    </span>
  );
}
