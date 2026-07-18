import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { repositoryApi, predictionApi } from '../services/apiServices';
import RiskBadge from '../components/RiskBadge';
import { SkeletonTable } from '../components/SkeletonLoader';
import { Zap, Clock, Activity, Search } from 'lucide-react';
import toast from 'react-hot-toast';

export default function PredictionsPage() {
  const [params] = useSearchParams();
  const [repos, setRepos] = useState<any[]>([]);
  const [repoId, setRepoId] = useState<number | null>(params.get('repoId') ? +params.get('repoId')! : null);
  const [preds, setPreds] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');

  useEffect(() => {
    repositoryApi.list().then(r => setRepos(r.data.data ?? []));
  }, []);

  useEffect(() => {
    if (!repoId) return;
    setLoading(true);
    predictionApi.getByRepository(repoId)
      .then(r => setPreds(r.data.data ?? []))
      .catch(() => toast.error('Failed to load predictions'))
      .finally(() => setLoading(false));
  }, [repoId]);

  const filtered = preds.filter(p => 
    !search || p.pullRequestId?.toString().includes(search)
  );

  return (
    <div className="p-6 space-y-5 animate-fade-in">
      <h1 className="text-2xl font-bold text-white">ML Predictions</h1>

      {/* Filters bar */}
      <div className="flex flex-wrap gap-3">
        {/* Repo selector */}
        <select
          className="input w-56"
          value={repoId ?? ''}
          onChange={e => setRepoId(+e.target.value || null)}
        >
          <option value="">Select repository…</option>
          {repos.map(r => <option key={r.id} value={r.id}>{r.fullName}</option>)}
        </select>

        {/* Search */}
        <div className="relative flex-1 min-w-[200px]">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"/>
          <input 
            className="input pl-9 text-sm" 
            placeholder="Search by PR ID..." 
            value={search} 
            onChange={e => setSearch(e.target.value)}
          />
        </div>
      </div>

      {/* Table */}
      {!repoId ? (
        <div className="card p-10 text-center text-slate-500">Select a repository to view predictions.</div>
      ) : loading ? <SkeletonTable rows={7}/> : (
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-surface-border">
                {['PR ID', 'Risk Level', 'Risk Score', 'Priority', 'Est. Review Time', 'Repo Health Score'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-border">
              {filtered.map((pred: any) => (
                <tr key={pred.id} className="hover:bg-surface-hover transition-colors">
                  <td className="px-4 py-3 text-slate-500 font-mono text-xs">#{pred.pullRequestId}</td>
                  <td className="px-4 py-3">
                    <RiskBadge level={pred.riskLevel}/>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <div className="w-16 h-2 rounded-full bg-surface-border overflow-hidden">
                        <div 
                          className={`h-full ${pred.riskScore > 0.6 ? 'bg-risk-high' : pred.riskScore > 0.3 ? 'bg-risk-medium' : 'bg-risk-low'}`}
                          style={{ width: `${Math.round(pred.riskScore * 100)}%` }}
                        />
                      </div>
                      <span className="text-xs font-mono text-slate-400">{(pred.riskScore * 100).toFixed(1)}%</span>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-slate-400 text-xs">
                    <span className="px-2 py-1 rounded bg-surface-border text-slate-300 font-medium">
                      {pred.priority?.replace('_', ' ') ?? '—'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-400 text-xs flex items-center gap-1">
                    <Clock size={12}/> {pred.estimatedReviewTimeHours?.toFixed(1) ?? '—'} hrs
                  </td>
                  <td className="px-4 py-3 text-slate-400 text-xs">
                    <div className="flex items-center gap-1">
                      <Activity size={12} className="text-brand-400"/>
                      {pred.repositoryHealthScore ? `${(pred.repositoryHealthScore * 100).toFixed(0)}%` : '—'}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {filtered.length === 0 && (
            <div className="p-8 text-center text-slate-500">No predictions found for this repository.</div>
          )}
        </div>
      )}
    </div>
  );
}
