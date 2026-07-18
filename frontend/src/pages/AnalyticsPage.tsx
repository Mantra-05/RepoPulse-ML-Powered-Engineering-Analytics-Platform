import { useEffect, useState } from 'react';
import { repositoryApi, predictionApi, pullRequestApi } from '../services/apiServices';
import {
  Chart as ChartJS,
  ArcElement, Tooltip, Legend, CategoryScale, LinearScale,
  BarElement, PointElement, LineElement, Filler
} from 'chart.js';
import { Doughnut, Bar, Line } from 'react-chartjs-2';
import { SkeletonCard } from '../components/SkeletonLoader';

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, PointElement, LineElement, Filler);

const CHART_DEFAULTS = {
  responsive: true,
  plugins: { legend: { labels: { color: '#94a3b8', font: { size: 11 } } } },
};

export default function AnalyticsPage() {
  const [repos, setRepos]         = useState<any[]>([]);
  const [repoId, setRepoId]       = useState<number | null>(null);
  const [analysis, setAnalysis]   = useState<any>(null);
  const [preds, setPreds]         = useState<any[]>([]);
  const [prs, setPrs]             = useState<any[]>([]);
  const [loading, setLoading]     = useState(false);

  useEffect(() => { repositoryApi.list().then(r => setRepos(r.data.data ?? [])); }, []);

  useEffect(() => {
    if (!repoId) return;
    setLoading(true);
    Promise.all([
      repositoryApi.analysis(repoId).catch(() => null),
      predictionApi.getByRepository(repoId).catch(() => ({ data: { data: [] } })),
      pullRequestApi.list(repoId).catch(() => ({ data: { data: [] } })),
    ]).then(([anaRes, predRes, prRes]) => {
      setAnalysis(anaRes?.data?.data ?? null);
      setPreds(predRes.data.data ?? []);
      setPrs(prRes.data.data ?? []);
    }).finally(() => setLoading(false));
  }, [repoId]);

  // ── Chart data ─────────────────────────────────────────────────────────────

  // 1. Health Gauge (doughnut)
  const healthScore = analysis?.healthScore ?? 0;
  const healthData = {
    datasets: [{
      data: [healthScore, 1 - healthScore],
      backgroundColor: [healthScore > 0.7 ? '#22c55e' : healthScore > 0.4 ? '#f59e0b' : '#ef4444', '#1e293b'],
      borderWidth: 0,
      cutout: '75%',
    }],
    labels: ['Health', ''],
  };

  // 2. Risk distribution pie
  const riskCounts = { LOW: 0, MEDIUM: 0, HIGH: 0 };
  preds.forEach(p => { if (p.riskLevel in riskCounts) riskCounts[p.riskLevel as keyof typeof riskCounts]++; });
  const riskData = {
    labels: ['Low', 'Medium', 'High'],
    datasets: [{ data: [riskCounts.LOW, riskCounts.MEDIUM, riskCounts.HIGH],
      backgroundColor: ['#22c55e', '#f59e0b', '#ef4444'], borderWidth: 0 }],
  };

  // 3. Merge success bar (by state)
  const stateMap = { OPEN: 0, CLOSED: 0, MERGED: 0 };
  prs.forEach((pr: any) => { if (pr.state in stateMap) stateMap[pr.state as keyof typeof stateMap]++; });
  const mergeData = {
    labels: ['Open', 'Closed', 'Merged'],
    datasets: [{
      label: 'PRs',
      data: [stateMap.OPEN, stateMap.CLOSED, stateMap.MERGED],
      backgroundColor: ['#6366f1', '#475569', '#22c55e'],
      borderRadius: 6,
    }],
  };

  // 4. Review Time Trend (last N PRs that have review times)
  const donePrs = prs.filter((pr: any) => pr.openedAt && (pr.closedAt || pr.mergedAt)).slice(-15);
  const reviewTrendData = {
    labels: donePrs.map((pr: any) => `#${pr.githubPrNumber}`),
    datasets: [{
      label: 'Review time (hrs)',
      data: donePrs.map((pr: any) => {
        const end = new Date(pr.mergedAt ?? pr.closedAt).getTime();
        return Math.round((end - new Date(pr.openedAt).getTime()) / 3600000);
      }),
      borderColor: '#6366f1',
      backgroundColor: 'rgba(99,102,241,0.1)',
      fill: true,
      tension: 0.4,
      pointRadius: 3,
    }],
  };

  // 5. Contributor activity (risk scores per prediction)
  const contribData = {
    labels: preds.slice(-12).map((p: any, i) => `PR #${i+1}`),
    datasets: [{
      label: 'Risk Score',
      data: preds.slice(-12).map((p: any) => +(p.riskScore * 100).toFixed(1)),
      borderColor: '#f59e0b',
      backgroundColor: 'rgba(245,158,11,0.1)',
      fill: true,
      tension: 0.4,
      pointRadius: 4,
      pointBackgroundColor: '#f59e0b',
    }],
  };

  const axisColor = '#475569';
  const gridColor = '#1e293b';
  const barOpts = { ...CHART_DEFAULTS, scales: { x: { ticks: { color: axisColor }, grid: { color: gridColor } }, y: { ticks: { color: axisColor }, grid: { color: gridColor } } } };

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-2xl font-bold text-white">Analytics</h1>
        <select className="input w-56" value={repoId ?? ''} onChange={e => setRepoId(+e.target.value || null)}>
          <option value="">Select repository…</option>
          {repos.map(r => <option key={r.id} value={r.id}>{r.fullName}</option>)}
        </select>
      </div>

      {!repoId ? (
        <div className="card p-10 text-center text-slate-500">Select a repository to view charts.</div>
      ) : loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {Array.from({length:4}).map((_,i) => <SkeletonCard key={i} className="h-64"/>)}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">

          {/* 1. Health Gauge */}
          <div className="card p-5">
            <h3 className="text-sm font-semibold text-slate-300 mb-4">Repository Health</h3>
            <div className="relative flex items-center justify-center h-44">
              <Doughnut data={healthData} options={{ ...CHART_DEFAULTS, plugins: { legend: { display: false } } }}/>
              <div className="absolute text-center">
                <p className="text-3xl font-bold text-white">{(healthScore*100).toFixed(0)}%</p>
                <p className="text-xs text-slate-500">Health Score</p>
              </div>
            </div>
          </div>

          {/* 2. Risk Distribution */}
          <div className="card p-5">
            <h3 className="text-sm font-semibold text-slate-300 mb-4">Risk Distribution</h3>
            <div className="flex items-center justify-center h-44">
              {preds.length === 0
                ? <p className="text-slate-500 text-sm">No predictions yet</p>
                : <Doughnut data={riskData} options={{ ...CHART_DEFAULTS, plugins: { legend: { position: 'right', labels: { color: '#94a3b8', font: { size: 11 }, boxWidth: 10 } } } }}/>
              }
            </div>
          </div>

          {/* 3. Merge Success */}
          <div className="card p-5">
            <h3 className="text-sm font-semibold text-slate-300 mb-4">PR State Distribution</h3>
            <div className="h-44">
              <Bar data={mergeData} options={barOpts}/>
            </div>
          </div>

          {/* 4. Review Time Trend */}
          <div className="card p-5 md:col-span-2">
            <h3 className="text-sm font-semibold text-slate-300 mb-4">Review Time Trend (last 15 PRs)</h3>
            <div className="h-48">
              {donePrs.length === 0
                ? <p className="text-slate-500 text-sm mt-8 text-center">Not enough data</p>
                : <Line data={reviewTrendData} options={barOpts}/>
              }
            </div>
          </div>

          {/* 5. Risk Score Activity */}
          <div className="card p-5">
            <h3 className="text-sm font-semibold text-slate-300 mb-4">Risk Score Activity</h3>
            <div className="h-48">
              {preds.length === 0
                ? <p className="text-slate-500 text-sm mt-8 text-center">No predictions yet</p>
                : <Line data={contribData} options={barOpts}/>
              }
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
