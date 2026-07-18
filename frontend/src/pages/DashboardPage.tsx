import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { repositoryApi, predictionApi } from '../services/apiServices';
import { SkeletonCard } from '../components/SkeletonLoader';
import { Star, GitFork, AlertCircle, Activity, TrendingUp, Users, Zap } from 'lucide-react';

interface StatCardProps { label: string; value: string | number; icon: React.ElementType; color: string; delta?: string; }
function StatCard({ label, value, icon: Icon, color, delta }: StatCardProps) {
  return (
    <div className="stat-card animate-fade-in">
      <div className="flex items-center justify-between mb-3">
        <span className="text-xs text-slate-500 font-medium uppercase tracking-wide">{label}</span>
        <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${color}`}>
          <Icon size={15} className="text-white" />
        </div>
      </div>
      <p className="text-2xl font-bold text-white">{value}</p>
      {delta && <p className="text-xs text-slate-500 mt-1">{delta}</p>}
    </div>
  );
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [repos, setRepos]   = useState<any[]>([]);
  const [preds, setPreds]   = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([repositoryApi.list(), ...[] ])
      .then(([reposRes]) => {
        setRepos(reposRes.data.data ?? []);
      })
      .finally(() => setLoading(false));
  }, []);

  const totalStars  = repos.reduce((s, r) => s + (r.starsCount ?? 0), 0);
  const totalForks  = repos.reduce((s, r) => s + (r.forksCount ?? 0), 0);
  const totalIssues = repos.reduce((s, r) => s + (r.openIssuesCount ?? 0), 0);

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white">
          Welcome back, <span className="text-gradient">{user?.username}</span> 👋
        </h1>
        <p className="text-slate-400 text-sm mt-1">Here's your engineering analytics overview</p>
      </div>

      {/* Stats grid */}
      {loading ? (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {Array.from({length:4}).map((_,i) => <SkeletonCard key={i} className="h-28"/>)}
        </div>
      ) : (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard label="Repositories"   value={repos.length}  icon={Activity}   color="bg-brand-600" />
          <StatCard label="Total Stars"    value={totalStars}    icon={Star}       color="bg-amber-500" delta="across all repos" />
          <StatCard label="Total Forks"    value={totalForks}    icon={GitFork}    color="bg-emerald-600" />
          <StatCard label="Open Issues"    value={totalIssues}   icon={AlertCircle} color="bg-red-600" />
        </div>
      )}

      {/* Recent repositories */}
      <div>
        <h2 className="text-lg font-semibold text-white mb-3 flex items-center gap-2">
          <TrendingUp size={18} className="text-brand-400"/>
          Recent Repositories
        </h2>
        {loading ? (
          <SkeletonCard className="h-48" />
        ) : repos.length === 0 ? (
          <div className="card p-8 text-center">
            <Zap size={32} className="text-slate-600 mx-auto mb-3" />
            <p className="text-slate-400">No repositories tracked yet.</p>
            <p className="text-slate-500 text-sm mt-1">Add a GitHub repository to get started.</p>
          </div>
        ) : (
          <div className="card divide-y divide-surface-border">
            {repos.slice(0, 5).map((repo: any) => (
              <div key={repo.id} className="flex items-center gap-4 p-4 hover:bg-surface-hover transition-colors">
                <div className="w-10 h-10 rounded-lg bg-brand-700/30 flex items-center justify-center">
                  <Activity size={16} className="text-brand-400"/>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-white truncate">{repo.fullName}</p>
                  <p className="text-xs text-slate-500 truncate">{repo.description ?? 'No description'}</p>
                </div>
                <div className="flex items-center gap-3 text-xs text-slate-500">
                  <span className="flex items-center gap-1"><Star size={11}/>{repo.starsCount}</span>
                  <span className="flex items-center gap-1"><GitFork size={11}/>{repo.forksCount}</span>
                </div>
                {repo.language && (
                  <span className="px-2 py-0.5 rounded-full bg-brand-700/30 text-brand-400 text-xs">{repo.language}</span>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
