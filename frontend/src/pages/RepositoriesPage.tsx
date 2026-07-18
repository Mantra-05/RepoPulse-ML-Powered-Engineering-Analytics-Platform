import { useEffect, useState } from 'react';
import { repositoryApi } from '../services/apiServices';
import toast from 'react-hot-toast';
import { Plus, RefreshCw, Trash2, Star, GitFork, ExternalLink, Search } from 'lucide-react';
import { SkeletonTable } from '../components/SkeletonLoader';
import { Spinner } from '../components/Spinner';
import { Link } from 'react-router-dom';

export default function RepositoriesPage() {
  const [repos, setRepos]     = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding]   = useState(false);
  const [fullName, setFullName] = useState('');
  const [search, setSearch]   = useState('');
  const [syncing, setSyncing] = useState<number | null>(null);

  const load = () => {
    setLoading(true);
    repositoryApi.list()
      .then(r => setRepos(r.data.data ?? []))
      .catch(() => toast.error('Failed to load repositories'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleAdd = async () => {
    if (!fullName.trim()) { toast.error('Enter owner/repo'); return; }
    setAdding(true);
    try {
      await repositoryApi.add(fullName.trim());
      toast.success(`Added ${fullName}`);
      setFullName('');
      load();
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Failed to add repository');
    } finally { setAdding(false); }
  };

  const handleDelete = async (id: number, name: string) => {
    if (!confirm(`Remove ${name}?`)) return;
    try {
      await repositoryApi.delete(id);
      toast.success(`Removed ${name}`);
      setRepos(prev => prev.filter(r => r.id !== id));
    } catch { toast.error('Failed to remove'); }
  };

  const handleSync = async (id: number) => {
    setSyncing(id);
    try {
      await repositoryApi.sync(id);
      toast.success('Sync complete');
      load();
    } catch { toast.error('Sync failed'); } finally { setSyncing(null); }
  };

  const filtered = repos.filter(r =>
    r.fullName?.toLowerCase().includes(search.toLowerCase()) ||
    r.description?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Repositories</h1>
          <p className="text-slate-400 text-sm mt-0.5">{repos.length} tracked repositories</p>
        </div>
      </div>

      {/* Add + Search */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"/>
          <input
            className="input pl-9"
            placeholder="Search repositories…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
        <div className="flex gap-2">
          <input
            className="input w-56"
            placeholder="owner/repo"
            value={fullName}
            onChange={e => setFullName(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleAdd()}
          />
          <button onClick={handleAdd} disabled={adding} className="btn-primary shrink-0">
            {adding ? <Spinner size={14}/> : <><Plus size={15}/>Add</>}
          </button>
        </div>
      </div>

      {/* List */}
      {loading ? <SkeletonTable rows={6}/> : filtered.length === 0 ? (
        <div className="card p-10 text-center text-slate-500">
          {search ? 'No results match your search.' : 'No repositories yet. Add one above.'}
        </div>
      ) : (
        <div className="card divide-y divide-surface-border">
          {filtered.map(repo => (
            <div key={repo.id} className="p-4 hover:bg-surface-hover transition-colors flex items-center gap-4">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-0.5">
                  <Link to={`/repositories/${repo.id}`} className="text-sm font-semibold text-brand-400 hover:text-brand-300 truncate">
                    {repo.fullName}
                  </Link>
                  {repo.language && (
                    <span className="px-1.5 py-0.5 rounded text-[10px] bg-brand-700/30 text-brand-400 shrink-0">{repo.language}</span>
                  )}
                </div>
                <p className="text-xs text-slate-500 truncate">{repo.description ?? '—'}</p>
              </div>
              <div className="flex items-center gap-3 text-xs text-slate-500 shrink-0">
                <span className="flex items-center gap-1"><Star size={11}/>{repo.starsCount ?? 0}</span>
                <span className="flex items-center gap-1"><GitFork size={11}/>{repo.forksCount ?? 0}</span>
              </div>
              <div className="flex items-center gap-1 shrink-0">
                <a href={repo.githubUrl} target="_blank" rel="noopener noreferrer" className="btn-secondary p-2">
                  <ExternalLink size={13}/>
                </a>
                <button onClick={() => handleSync(repo.id)} className="btn-secondary p-2" disabled={syncing === repo.id}>
                  <RefreshCw size={13} className={syncing === repo.id ? 'animate-spin' : ''}/>
                </button>
                <button onClick={() => handleDelete(repo.id, repo.fullName)} className="btn-secondary p-2 hover:text-red-400">
                  <Trash2 size={13}/>
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
