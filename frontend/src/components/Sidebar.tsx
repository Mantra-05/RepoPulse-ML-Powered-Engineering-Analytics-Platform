import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, GitPullRequest, BarChart3, Zap,
  BookOpen, LogOut, Menu, X, ChevronRight
} from 'lucide-react';
import { useState } from 'react';
import clsx from 'clsx';

const nav = [
  { to: '/',             label: 'Dashboard',    icon: LayoutDashboard },
  { to: '/repositories', label: 'Repositories', icon: BookOpen },
  { to: '/pull-requests',label: 'Pull Requests',icon: GitPullRequest },
  { to: '/analytics',    label: 'Analytics',    icon: BarChart3 },
  { to: '/predictions',  label: 'Predictions',  icon: Zap },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate         = useNavigate();
  const [open, setOpen]  = useState(false);

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <>
      {/* Mobile toggle */}
      <button
        onClick={() => setOpen(!open)}
        className="fixed top-4 left-4 z-50 lg:hidden btn-secondary p-2"
      >
        {open ? <X size={18}/> : <Menu size={18}/>}
      </button>

      {/* Backdrop */}
      {open && (
        <div
          className="fixed inset-0 z-30 bg-black/60 lg:hidden"
          onClick={() => setOpen(false)}
        />
      )}

      {/* Sidebar panel */}
      <aside className={clsx(
        'fixed top-0 left-0 h-screen z-40 flex flex-col',
        'w-64 bg-surface-card border-r border-surface-border',
        'transition-transform duration-300 ease-in-out',
        open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
      )}>
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 py-5 border-b border-surface-border">
          <div className="w-9 h-9 rounded-xl bg-brand-600 flex items-center justify-center shadow-glow">
            <Zap size={18} className="text-white" />
          </div>
          <div>
            <h1 className="font-bold text-white text-sm">RepoPulse</h1>
            <p className="text-[10px] text-slate-500">ML Engineering Analytics</p>
          </div>
        </div>

        {/* Nav links */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {nav.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              onClick={() => setOpen(false)}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium',
                'transition-all duration-200 group',
                isActive
                  ? 'bg-brand-600/20 text-brand-400 glow-border'
                  : 'text-slate-400 hover:bg-surface-hover hover:text-slate-200'
              )}
            >
              {({ isActive }) => (
                <>
                  <Icon size={17} className={isActive ? 'text-brand-400' : 'text-slate-500 group-hover:text-slate-300'} />
                  <span className="flex-1">{label}</span>
                  {isActive && <ChevronRight size={13} className="text-brand-400" />}
                </>
              )}
            </NavLink>
          ))}
        </nav>

        {/* User section */}
        <div className="px-3 py-4 border-t border-surface-border space-y-1">
          <div className="flex items-center gap-3 px-3 py-2.5 rounded-xl bg-surface-hover">
            <div className="w-8 h-8 rounded-full bg-brand-700 flex items-center justify-center text-sm font-bold text-brand-300">
              {user?.username?.[0]?.toUpperCase() ?? 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">{user?.username}</p>
              <p className="text-xs text-slate-500 truncate">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm text-slate-400
                       hover:bg-red-500/10 hover:text-red-400 transition-all duration-200"
          >
            <LogOut size={16}/> Sign out
          </button>
        </div>
      </aside>
    </>
  );
}
