import { useState, FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../services/apiServices';
import toast from 'react-hot-toast';
import { Zap } from 'lucide-react';
import { Spinner } from '../components/Spinner';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.username || !form.email || !form.password) { toast.error('All fields required'); return; }
    setLoading(true);
    try {
      const { data } = await authApi.register(form);
      localStorage.setItem('accessToken',  data.data.accessToken);
      localStorage.setItem('refreshToken', data.data.refreshToken);
      toast.success('Account created!');
      navigate('/');
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-32 left-1/2 w-96 h-96 bg-brand-600/10 rounded-full blur-3xl" />
      </div>
      <div className="w-full max-w-md relative animate-fade-in">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-brand-600 shadow-glow mb-4">
            <Zap size={24} className="text-white" />
          </div>
          <h1 className="text-3xl font-bold text-white">RepoPulse</h1>
          <p className="text-slate-400 mt-1 text-sm">Create your account</p>
        </div>
        <div className="card p-8 glow-border">
          <form onSubmit={handleSubmit} className="space-y-4">
            {(['username', 'email', 'password'] as const).map(field => (
              <div key={field}>
                <label className="block text-xs font-medium text-slate-400 mb-1.5 capitalize">{field}</label>
                <input
                  className="input"
                  type={field === 'password' ? 'password' : field === 'email' ? 'email' : 'text'}
                  placeholder={field === 'email' ? 'you@example.com' : field === 'password' ? '••••••••' : `your_${field}`}
                  value={form[field]}
                  onChange={e => setForm(prev => ({ ...prev, [field]: e.target.value }))}
                />
              </div>
            ))}
            <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-3 mt-2">
              {loading ? <Spinner size={16}/> : 'Create Account'}
            </button>
          </form>
          <p className="text-center text-sm text-slate-500 mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-brand-400 hover:text-brand-300 font-medium">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
