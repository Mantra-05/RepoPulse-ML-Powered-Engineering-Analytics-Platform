import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { repositoryApi } from '../services/apiServices';
import toast from 'react-hot-toast';
import { SkeletonCard } from '../components/SkeletonLoader';
import { Star, GitFork, AlertCircle, RefreshCw, BarChart3, Activity } from 'lucide-react';
import { Spinner } from '../components/Spinner';

export default function RepositoryDetailPage() {
  const { id }           = useParams<{ id: string }>();
  const [repo, setRepo]  = useState<any>(null);
  const [analysis, setAnalysis] = useState<any>(null);
  const [loading, setLoading]   = useState(true);
  const [analyzing, setAnalyzing] = useState(false);

  useEffect(() => {
    if (!id) return;
    Promise.all([repositoryApi.get(+id), repositoryApi.analysis(+id).catch(() => null)])
      .then(([repoRes, anaRes]) => {
        setRepo(repoRes.data.data);
        if (anaRes) setAnalysis(anaRes.data.data);
      })
      .finally(() => setLoading(false));
  }, [id]);

  const runAnalysis = async () => {
    setAnalyzing(true);
    try {
      const res = await repositoryApi.runAnalysis(+id!);
      setAnalysis(res.data.data);
      toast.success('Analysis complete');
    } catch { toast.error('Analysis failed'); } finally { setAnalyzing(false); }
  };

  if (loading) return <div className="p-6 grid gap-4"><SkeletonCard className="h-40"/><SkeletonCard className="h-64"/></div>;
  if (!repo)   return <div className="p-6 text-slate-400">Repository not found.</div>;

  const metrics = [
    { label: 'Avg PR Size',         value: analysis?.avgPrSize?.toFixed(0) ?? '—', unit: 'lines' },
    { label: 'Avg Review Time',     value: analysis?.avgReviewTimeHours?.toFixed(1) ?? '—', unit: 'hrs' },
    { label: 'Avg Commits/PR',      value: analysis?.avgCommitsPerPr?.toFixed(1) ?? '—', unit: '' },
    { label: 'Avg Files Changed',   value: analysis?.avgFilesChanged?.toFixed(1) ?? '—', unit: '' },
    { label: 'Merge Rate',          value: analysis ? `${(analysis.mergeRate * 100).toFixed(0)}%` : '—', unit: '' },
    { label: 'Health Score',        value: analysis ? `${(analysis.healthScore * 100).toFixed(0)}%` : '—', unit: '' },
    { label: 'Open PRs',            value: analysis?.openPrCount ?? '—', unit: '' },
    { label: 'Contributors',        value: analysis?.contributorCount ?? '—', unit: '' },
  ];

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      {/* Header */}
      <div className="card p-6">
        <div className="flex items-start justify-between gap-4 flex-wrap">
          <div>
            <h1 className="text-xl font-bold text-white">{repo.fullName}</h1>
            <p className="text-slate-400 text-sm mt-1">{repo.description ?? 'No description'}</p>
          </div>
          <div className="flex items-center gap-3 text-sm text-slate-400">
            <span className="flex items-center gap-1"><Star size={13} className="text-amber-400"/>{repo.starsCount}</span>
            <span className="flex items-center gap-1"><GitFork size={13} className="text-emerald-400"/>{repo.forksCount}</span>
            <span className="flex items-center gap-1"><AlertCircle size={13} className="text-red-400"/>{repo.openIssuesCount}</span>
            {repo.language && <span className="px-2 py-0.5 rounded bg-brand-700/30 text-brand-400 text-xs">{repo.language}</span>}
          </div>
        </div>
      </div>

      {/* Analysis */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-lg font-semibold text-white flex items-center gap-2">
            <BarChart3 size={17} className="text-brand-400"/> Analysis Metrics
          </h2>
          <button onClick={runAnalysis} disabled={analyzing} className="btn-secondary text-xs">
            {analyzing ? <Spinner size={13}/> : <><RefreshCw size={13}/>Refresh</>}
          </button>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {metrics.map(m => (
            <div key={m.label} className="stat-card">
              <p className="text-xs text-slate-500 mb-1">{m.label}</p>
              <p className="text-xl font-bold text-white">{m.value} <span className="text-xs text-slate-500">{m.unit}</span></p>
            </div>
          ))}
        </div>
      </div>

      {/* Quick links */}
      <div className="flex gap-3 flex-wrap">
        <Link to={`/pull-requests?repoId=${id}`} className="btn-secondary">
          <Activity size={14}/> Pull Requests
        </Link>
        <Link to={`/predictions?repoId=${id}`} className="btn-secondary">
          <BarChart3 size={14}/> Predictions
        </Link>
      </div>
    </div>
  );
}
