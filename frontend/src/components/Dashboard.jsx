import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import {
  FolderGit2,
  Plus,
  Github,
  Upload,
  Trash2,
  ExternalLink,
  Clock,
  CheckCircle2,
  AlertTriangle,
  Loader2,
  X,
  FileCode2,
} from 'lucide-react';

const Dashboard = () => {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);

  // New Project Form State
  const [activeTab, setActiveTab] = useState('github'); // 'github' | 'upload'
  const [repoUrl, setRepoUrl] = useState('');
  const [repoName, setRepoName] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const navigate = useNavigate();

  const [aiHealth, setAiHealth] = useState({ status: 'checking', message: 'Checking AI...' });

  const fetchProjects = async (silent = false) => {
    if (!silent) setLoading(true);
    setError('');
    try {
      const response = await api.get('/api/projects');
      setProjects(response.data || []);
    } catch (err) {
      if (!silent) setError(err.response?.data?.message || 'Failed to load projects');
    } finally {
      if (!silent) setLoading(false);
    }
  };

  // Check AI Health
  useEffect(() => {
    const checkAi = async () => {
      try {
        const res = await api.get('/api/ai/health');
        setAiHealth({ status: 'UP', message: 'Gemini Active' });
      } catch (err) {
        setAiHealth({ status: 'DOWN', message: 'AI Offline' });
      }
    };
    checkAi();
  }, []);

  useEffect(() => {
    fetchProjects();
  }, []);

  // Poll for processing projects every 3 seconds until completed or failed
  useEffect(() => {
    const hasProcessing = projects.some((p) => (p.status || '').toLowerCase() === 'processing');
    if (!hasProcessing) return;

    const interval = setInterval(() => {
      fetchProjects(true);
    }, 3000);

    return () => clearInterval(interval);
  }, [projects]);

  const handleCreateProject = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      if (activeTab === 'github') {
        await api.post('/api/projects', { repoUrl, repoName });
      } else {
        if (!selectedFile) {
          throw new Error('Please select a ZIP file to upload');
        }
        const formData = new FormData();
        formData.append('file', selectedFile);
        await api.post('/api/projects/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
      }

      setIsModalOpen(false);
      setRepoUrl('');
      setRepoName('');
      setSelectedFile(null);
      await fetchProjects();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to create project');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteProject = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete "${name}"?`)) return;

    try {
      await api.delete(`/api/projects/${id}`);
      setProjects((prev) => prev.filter((p) => p.id !== id));
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete project');
    }
  };

  const getStatusBadge = (status) => {
    const st = (status || 'processing').toLowerCase();
    if (st === 'completed') {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
          <CheckCircle2 className="w-3.5 h-3.5" />
          Completed
        </span>
      );
    }
    if (st === 'failed') {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-red-500/10 text-red-400 border border-red-500/20">
          <AlertTriangle className="w-3.5 h-3.5" />
          Failed
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20">
        <Loader2 className="w-3.5 h-3.5 animate-spin" />
        Processing
      </span>
    );
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-slate-950 text-slate-100 p-6 md:p-10">
      <div className="max-w-7xl mx-auto">
        {/* Header Banner */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-bold text-white tracking-tight">Project Dashboard</h1>
            <p className="text-slate-400 text-sm mt-1">
              Generate, visualize, and query documentation for your codebases
            </p>
          </div>
          <div className="flex items-center gap-3 self-start md:self-auto">
            <span
              className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border ${
                aiHealth.status === 'UP'
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                  : aiHealth.status === 'DOWN'
                  ? 'bg-red-500/10 text-red-400 border-red-500/20'
                  : 'bg-slate-800 text-slate-400 border-slate-700'
              }`}
            >
              <span
                className={`w-2 h-2 rounded-full ${
                  aiHealth.status === 'UP' ? 'bg-emerald-400' : aiHealth.status === 'DOWN' ? 'bg-red-400' : 'bg-slate-500'
                }`}
              />
              {aiHealth.message}
            </span>
            <button
              onClick={() => setIsModalOpen(true)}
              className="flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white font-medium px-4 py-2.5 rounded-xl transition shadow-lg shadow-blue-600/25"
            >
              <Plus className="w-5 h-5" />
              New Project
            </button>
          </div>
        </div>

        {/* Content Section */}
        {loading ? (
          <div className="flex flex-col items-center justify-center py-20 text-slate-400">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500 mb-3" />
            <p className="text-sm">Loading your projects...</p>
          </div>
        ) : error && projects.length === 0 ? (
          <div className="p-6 bg-red-500/10 border border-red-500/20 rounded-2xl text-center text-red-400 max-w-md mx-auto">
            <AlertTriangle className="w-8 h-8 mx-auto mb-2" />
            <p className="font-semibold">{error}</p>
            <button
              onClick={fetchProjects}
              className="mt-4 text-xs font-medium bg-red-500/20 hover:bg-red-500/30 px-3 py-1.5 rounded-lg transition"
            >
              Try Again
            </button>
          </div>
        ) : projects.length === 0 ? (
          <div className="bg-slate-900/50 border border-slate-800 border-dashed rounded-2xl p-12 text-center max-w-xl mx-auto">
            <div className="w-16 h-16 rounded-2xl bg-blue-600/10 border border-blue-500/20 flex items-center justify-center text-blue-400 mx-auto mb-4">
              <FolderGit2 className="w-8 h-8" />
            </div>
            <h3 className="text-lg font-semibold text-white mb-1">No Projects Found</h3>
            <p className="text-sm text-slate-400 mb-6">
              Create your first project by adding a GitHub repository URL or uploading a codebase ZIP file.
            </p>
            <button
              onClick={() => setIsModalOpen(true)}
              className="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white font-medium px-4 py-2 rounded-xl transition"
            >
              <Plus className="w-4 h-4" />
              Add Project
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {projects.map((project) => (
              <div
                key={project.id}
                className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-2xl p-6 transition flex flex-col justify-between shadow-lg shadow-slate-950/30 group"
              >
                <div>
                  <div className="flex items-start justify-between gap-3 mb-3">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400">
                        <FileCode2 className="w-5 h-5" />
                      </div>
                      <div>
                        <h3 className="text-base font-bold text-white group-hover:text-blue-400 transition truncate max-w-[180px]">
                          {project.repoName || 'Untitled Project'}
                        </h3>
                        <span className="text-xs text-slate-500 flex items-center gap-1 mt-0.5">
                          <Clock className="w-3 h-3" />
                          {project.createdAt ? new Date(project.createdAt).toLocaleDateString() : 'Recent'}
                        </span>
                      </div>
                    </div>
                    {getStatusBadge(project.status)}
                  </div>

                  <p className="text-xs text-slate-400 truncate bg-slate-950/60 p-2 rounded-lg border border-slate-800/80 mb-3 font-mono">
                    {project.repoUrl || 'Local Repository'}
                  </p>

                  {project.status === 'failed' && project.errorMessage && (
                    <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-2.5 rounded-xl text-xs mb-3 flex items-start gap-2">
                      <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" />
                      <span className="line-clamp-2">{project.errorMessage}</span>
                    </div>
                  )}
                </div>

                <div className="flex items-center justify-between border-t border-slate-800/80 pt-4 mt-2">
                  <button
                    onClick={() => handleDeleteProject(project.id, project.repoName)}
                    className="p-2 text-slate-500 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition"
                    title="Delete Project"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>

                  <button
                    onClick={() => navigate(`/projects/${project.id}`)}
                    className="flex items-center gap-1.5 text-xs font-semibold bg-blue-600/10 hover:bg-blue-600 text-blue-400 hover:text-white border border-blue-500/20 hover:border-blue-600 px-3.5 py-2 rounded-xl transition"
                  >
                    <span>View Details</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* New Project Modal */}
        {isModalOpen && (
          <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg overflow-hidden shadow-2xl animate-in fade-in zoom-in-95 duration-200">
              <div className="flex items-center justify-between p-6 border-b border-slate-800">
                <h3 className="text-lg font-bold text-white flex items-center gap-2">
                  <Plus className="w-5 h-5 text-blue-500" />
                  Create New Project
                </h3>
                <button
                  onClick={() => setIsModalOpen(false)}
                  className="p-1 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Tab Selector */}
              <div className="flex border-b border-slate-800 bg-slate-950/40 p-1.5 gap-1">
                <button
                  onClick={() => setActiveTab('github')}
                  className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-semibold rounded-xl transition ${
                    activeTab === 'github'
                      ? 'bg-blue-600 text-white shadow-md'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                  }`}
                >
                  <Github className="w-4 h-4" />
                  GitHub Repository
                </button>
                <button
                  onClick={() => setActiveTab('upload')}
                  className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-semibold rounded-xl transition ${
                    activeTab === 'upload'
                      ? 'bg-blue-600 text-white shadow-md'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                  }`}
                >
                  <Upload className="w-4 h-4" />
                  File Upload (.zip)
                </button>
              </div>

              <form onSubmit={handleCreateProject} className="p-6 space-y-4">
                {activeTab === 'github' ? (
                  <>
                    <div>
                      <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                        GitHub Repository URL
                      </label>
                      <input
                        type="url"
                        value={repoUrl}
                        onChange={(e) => setRepoUrl(e.target.value)}
                        placeholder="https://github.com/owner/repository"
                        className="w-full bg-slate-950 border border-slate-800 focus:border-blue-500 text-white rounded-xl px-4 py-2.5 text-sm placeholder:text-slate-600"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                        Project Name (Optional)
                      </label>
                      <input
                        type="text"
                        value={repoName}
                        onChange={(e) => setRepoName(e.target.value)}
                        placeholder="e.g. My Awesome App"
                        className="w-full bg-slate-950 border border-slate-800 focus:border-blue-500 text-white rounded-xl px-4 py-2.5 text-sm placeholder:text-slate-600"
                      />
                    </div>
                  </>
                ) : (
                  <div>
                    <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                      Upload Codebase ZIP File
                    </label>
                    <div className="border-2 border-dashed border-slate-800 hover:border-blue-500/50 bg-slate-950/60 rounded-xl p-6 text-center transition">
                      <input
                        type="file"
                        accept=".zip"
                        onChange={(e) => setSelectedFile(e.target.files[0])}
                        className="hidden"
                        id="zip-upload"
                        required
                      />
                      <label htmlFor="zip-upload" className="cursor-pointer flex flex-col items-center gap-2">
                        <Upload className="w-8 h-8 text-blue-500 mb-1" />
                        <span className="text-sm font-medium text-slate-300">
                          {selectedFile ? selectedFile.name : 'Click to browse ZIP file'}
                        </span>
                        <span className="text-xs text-slate-500">Supports .zip archives up to 50MB</span>
                      </label>
                    </div>
                  </div>
                )}

                <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
                  <button
                    type="button"
                    onClick={() => setIsModalOpen(false)}
                    className="px-4 py-2 text-xs font-semibold text-slate-400 hover:text-white rounded-xl hover:bg-slate-800 transition"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold px-5 py-2.5 rounded-xl transition shadow-lg shadow-blue-600/25 disabled:opacity-50"
                  >
                    {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
                    {submitting ? 'Creating Project...' : 'Create Project'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
