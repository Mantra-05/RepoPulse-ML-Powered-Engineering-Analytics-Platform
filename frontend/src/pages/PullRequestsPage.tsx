import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { repositoryApi, pullRequestApi, predictionApi } from '../services/apiServices';
import RiskBadge from '../components/RiskBadge';
import { SkeletonTable } from '../components/SkeletonLoader';
import { Spinner } from '../components/Spinner';
import toast from 'react-hot-toast';
import { GitPullRequest, Zap, ChevronLeft, ChevronRight, Search, Filter } from 'lucide-react';

const STATES = ['ALL', 'OPEN', 'CLOSED', 'MERGED'];
const RISKS  = ['ALL', 'LOW', 'MEDIUM', 'HIGH'];
const PAGE_SIZE = 10;

export default function PullRequestsPage() {
  const [params] = useSearchParams();
  const [repos, setRepos]   = useState<any[]>([]);
  const [repoId, setRepoId] = useState<number | null>(params.get('repoId') ? +params.get('repoId')! : null);
  const [prs, setPrs]       = useState<any[]>([]);
  const [preds, setPreds]   = useState<Record<number, any>>({});
  const [loading, setLoading] = useState(false);
  const [state, setState]   = useState('ALL');
  const [risk, setRisk]     = useState('ALL');
  const [search, setSearch] = useState('');
  const [page, setPage]     = useState(1);
  const [predicting, setPredicting] = useState<number | null>(null);

  useEffect(() => {
    repositoryApi.list().then(r => setRepos(r.data.data ?? []));
  }, []);

  useEffect(() => {
    if (!repoId) return;
    setLoading(true);
    const stateParam = state !== 'ALL' ? state.toLowerCase() : undefined;
    pullRequestApi.list(repoId, stateParam)
      .then(r => { setPrs(r.data.data ?? []); setPage(1); })
      .catch(() => toast.error('Failed to load pull requests'))
      .finally(() => setLoading(false));

    predictionApi.getByRepository(repoId)
      .then(r => {
        const map: Record<number, any> = {};
        (r.data.data ?? []).forEach((p: any) => { map[p.pullRequestId] = p; });
        setPreds(map);
      })
      .catch(() => {});
  }, [repoId, state]);

  const handlePredict = async (prId: number) => {
    setPredicting(prId);
    try {
      const r = await predictionApi.request(prId);
      setPreds(prev => ({ ...prev, [prId]: r.data.data }));
      toast.success('Prediction generated');
    } catch { toast.error('Prediction failed'); } finally { setPredicting(null); }
  };

  const filtered = prs.filter(pr => {
    const riskMatch = risk === 'ALL' || preds[pr.id]?.riskLevel === risk;
    const searchMatch = !search || pr.title?.toLowerCase().includes(search.toLowerCase())
      || pr.authorLogin?.toLowerCase().includes(search.toLowerCase());
    return riskMatch && searchMatch;
  });
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated  = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  return (
    <div className="p-6 space-y-5 animate-fade-in">
      <h1 className="text-2xl font-bold text-white">Pull Requests</h1>

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

        {/* State filter */}
        <div className="flex rounded-xl overflow-hidden border border-surface-border">
          {STATES.map(s => (
            <button key={s} onClick={() => { setState(s); setPage(1); }}
              className={`px-3 py-2 text-xs font-medium transition-colors ${state === s ? 'bg-brand-600 text-white' : 'text-slate-400 hover:bg-surface-hover'}`}>
              {s}
            </button>
          ))}
        </div>

        {/* Risk filter */}
        <div className="flex rounded-xl overflow-hidden border border-surface-border">
          {RISKS.map(r => (
            <button key={r} onClick={() => { setRisk(r); setPage(1); }}
              className={`px-3 py-2 text-xs font-medium transition-colors ${risk === r ? 'bg-brand-600 text-white' : 'text-slate-400 hover:bg-surface-hover'}`}>
              {r}
            </button>
          ))}
        </div>

        {/* Search */}
        <div className="relative flex-1 min-w-[200px]">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"/>
          <input className="input pl-9 text-sm" placeholder="Search PRs…" value={search} onChange={e => { setSearch(e.target.value); setPage(1); }}/>
        </div>
      </div>

      {/* Table */}
      {!repoId ? (
        <div className="card p-10 text-center text-slate-500">Select a repository to view pull requests.</div>
      ) : loading ? <SkeletonTable rows={7}/> : (
        <>
          <div className="card overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-surface-border">
                  {['#', 'Title', 'Author', 'State', 'Risk', '+/-', 'Files', 'Days Open', 'Action'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {paginated.map((pr: any) => {
                  const pred = preds[pr.id];
                  const daysOpen = pr.openedAt ? Math.round((Date.now() - new Date(pr.openedAt).getTime()) / 86400000) : '—';
                  return (
                    <tr key={pr.id} className="hover:bg-surface-hover transition-colors">
                      <td className="px-4 py-3 text-slate-500 font-mono text-xs">#{pr.githubPrNumber}</td>
                      <td className="px-4 py-3 max-w-xs">
                        <p className="text-white font-medium truncate">{pr.title}</p>
                      </td>
                      <td className="px-4 py-3 text-slate-400 text-xs">{pr.authorLogin ?? '—'}</td>
                      <td className="px-4 py-3">
                        <span className={`badge ${pr.state === 'OPEN' ? 'badge-low' : pr.state === 'MERGED' ? 'bg-brand-500/20 text-brand-400' : 'bg-slate-700/40 text-slate-400'}`}>
                          {pr.state}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        {pred ? <RiskBadge level={pred.riskLevel}/> : <span className="text-slate-600 text-xs">N/A</span>}
                      </td>
                      <td className="px-4 py-3 text-xs font-mono">
                        <span className="text-green-400">+{pr.additions ?? 0}</span>
                        <span className="text-slate-500"> / </span>
                        <span className="text-red-400">-{pr.deletions ?? 0}</span>
                      </td>
                      <td className="px-4 py-3 text-slate-400 text-xs">{pr.changedFiles ?? '—'}</td>
                      <td className="px-4 py-3 text-slate-400 text-xs">{daysOpen}</td>
                      <td className="px-4 py-3">
                        <button
                          onClick={() => handlePredict(pr.id)}
                          disabled={predicting === pr.id}
                          className="btn-secondary text-xs px-2 py-1"
                          title="Run ML prediction"
                        >
                          {predicting === pr.id ? <Spinner size={12}/> : <><Zap size={11}/>Predict</>}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
            {paginated.length === 0 && (
              <div className="p-8 text-center text-slate-500">No pull requests match your filters.</div>
            )}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between text-sm text-slate-400">
              <span>Showing {Math.min(filtered.length, (page-1)*PAGE_SIZE+1)}–{Math.min(filtered.length, page*PAGE_SIZE)} of {filtered.length}</span>
              <div className="flex gap-2">
                <button onClick={() => setPage(p => Math.max(1, p-1))} disabled={page===1} className="btn-secondary px-3 py-1.5 text-xs">
                  <ChevronLeft size={13}/>
                </button>
                <span className="px-3 py-1.5 text-xs bg-surface-card rounded-lg border border-surface-border">{page}/{totalPages}</span>
                <button onClick={() => setPage(p => Math.min(totalPages, p+1))} disabled={page===totalPages} className="btn-secondary px-3 py-1.5 text-xs">
                  <ChevronRight size={13}/>
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
