import api from '../lib/api';

export interface LoginRequest  { username: string; password: string; }
export interface RegisterRequest { username: string; email: string; password: string; }
export interface AuthResponse  { accessToken: string; refreshToken: string; tokenType: string; expiresIn: number; }

export const authApi = {
  login:    (data: LoginRequest)    => api.post<{ data: AuthResponse }>('/auth/login', data),
  register: (data: RegisterRequest) => api.post<{ data: AuthResponse }>('/auth/register', data),
  me:       ()                      => api.get('/auth/me'),
};

export interface Repository {
  id: number; githubId: number; name: string; fullName: string;
  description: string; language: string; starsCount: number;
  forksCount: number; openIssuesCount: number; githubUrl: string;
  lastSyncedAt: string; createdAt: string;
}

export const repositoryApi = {
  list:   ()       => api.get<{ data: Repository[] }>('/repositories'),
  get:    (id: number) => api.get<{ data: Repository }>(`/repositories/${id}`),
  add:    (fullName: string) => api.post('/repositories', { fullName }),
  delete: (id: number) => api.delete(`/repositories/${id}`),
  sync:   (id: number) => api.post(`/repositories/${id}/sync`),
  analysis: (id: number) => api.get(`/repositories/${id}/analysis`),
  runAnalysis: (id: number) => api.post(`/repositories/${id}/analysis`),
};

export const pullRequestApi = {
  list:   (repoId: number, state?: string) =>
    api.get(`/repositories/${repoId}/pull-requests${state ? `?state=${state}` : ''}`),
  get:    (repoId: number, prId: number) =>
    api.get(`/repositories/${repoId}/pull-requests/${prId}`),
};

export const predictionApi = {
  getByRepository: (repoId: number) => api.get(`/predictions/${repoId}`),
  getByPr:         (prId: number)   => api.get(`/predictions/pull-requests/${prId}`),
  request:         (prId: number)   => api.post(`/predictions/pull-requests/${prId}`),
};

export const contributorApi = {
  list: (repoId: number) => api.get(`/repositories/${repoId}/contributors`),
};

export const commitApi = {
  list: (repoId: number) => api.get(`/repositories/${repoId}/commits`),
};
