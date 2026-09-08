import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';
import GraphView from './GraphView';
import ChatInterface from './ChatInterface';
import {
  ArrowLeft,
  FileText,
  Network,
  MessageSquare,
  FileCode,
  CheckCircle2,
  Clock,
  Sparkles,
  ChevronRight,
  Code2,
  Loader2,
  X,
  Layers,
} from 'lucide-react';

const ProjectDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [project, setProject] = useState(null);
  const [graphNodes, setGraphNodes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [activeTab, setActiveTab] = useState('doc'); // 'doc' | 'graph' | 'chat'
  const [selectedNode, setSelectedNode] = useState(null);
  const [generatedDoc, setGeneratedDoc] = useState('');
  const [docLoading, setDocLoading] = useState(false);

  const fetchData = async (silent = false) => {
    if (!silent) setLoading(true);
    setError('');
    try {
      const [projRes, graphRes] = await Promise.all([
        api.get(`/api/projects/${id}`),
        api.get(`/api/graph/project/${id}`).catch(() => ({ data: [] })),
      ]);
      setProject(projRes.data);
      const nodes = graphRes.data || [];
      setGraphNodes(nodes);

      if (nodes.length > 0) {
        setSelectedNode((prev) => {
          if (!prev) return nodes[0];
          return nodes.find((n) => (n.path || n.id) === (prev.path || prev.id)) || nodes[0];
        });
      }
    } catch (err) {
      if (!silent) setError(err.response?.data?.message || err.message || 'Failed to load project details');
    } finally {
      if (!silent) setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  // Poll while processing
  useEffect(() => {
    if ((project?.status || '').toLowerCase() !== 'processing') return;

    const interval = setInterval(() => {
      fetchData(true);
    }, 3000);

    return () => clearInterval(interval);
  }, [project?.status, id]);

  const handleGenerateDocForNode = async (node) => {
    if (!node) return;
    setDocLoading(true);
    try {
      const response = await api.post('/api/ai/doc', {
        codeContext: node.content || '',
        prompt: `Generate complete Javadoc for ${node.name}`,
      });
      setGeneratedDoc(response.data.documentation || 'No documentation returned');
    } catch (err) {
      setGeneratedDoc('Failed to generate documentation: ' + (err.response?.data?.message || err.message));
    } finally {
      setDocLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-4rem)] bg-slate-950 flex flex-col items-center justify-center text-slate-400">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500 mb-3" />
        <p className="text-sm">Fetching project details & AST graph...</p>
      </div>
    );
  }

  if (error || !project) {
    return (
      <div className="min-h-[calc(100vh-4rem)] bg-slate-950 p-8 flex justify-center items-center">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 max-w-md text-center">
          <p className="text-red-400 font-semibold mb-4">{error || 'Project not found'}</p>
          <button
            onClick={() => navigate('/dashboard')}
            className="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold px-4 py-2 rounded-xl transition"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-slate-950 text-slate-100 p-6 md:p-10">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Breadcrumb & Top Bar */}
        <div className="flex items-center justify-between">
          <button
            onClick={() => navigate('/dashboard')}
            className="inline-flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-white bg-slate-900 border border-slate-800 hover:bg-slate-800 px-3.5 py-2 rounded-xl transition"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Projects
          </button>

          {project.status?.toLowerCase() === 'completed' ? (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <CheckCircle2 className="w-3.5 h-3.5" />
              Completed
            </span>
          ) : project.status?.toLowerCase() === 'failed' ? (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-red-500/10 text-red-400 border border-red-500/20">
              <Loader2 className="w-3.5 h-3.5" />
              Failed
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20">
              <Loader2 className="w-3.5 h-3.5 animate-spin" />
              Processing Repository
            </span>
          )}
        </div>

        {/* Status Notification Alerts */}
        {project.status?.toLowerCase() === 'processing' && (
          <div className="bg-amber-500/10 border border-amber-500/30 text-amber-300 px-5 py-3.5 rounded-2xl flex items-center gap-3 text-xs shadow-lg">
            <Loader2 className="w-4 h-4 animate-spin shrink-0 text-amber-400" />
            <div>
              <p className="font-semibold">Repository is being cloned and analyzed in the background...</p>
              <p className="text-amber-400/80 text-[11px] mt-0.5">AST files and interactive dependency graph will automatically appear once processing completes.</p>
            </div>
          </div>
        )}

        {project.status?.toLowerCase() === 'failed' && (
          <div className="bg-red-500/10 border border-red-500/30 text-red-300 px-5 py-3.5 rounded-2xl flex items-start gap-3 text-xs shadow-lg">
            <div className="w-2 h-2 rounded-full bg-red-400 shrink-0 mt-1.5" />
            <div>
              <p className="font-semibold">Repository processing failed</p>
              <p className="text-red-400/90 text-[11px] mt-0.5">{project.errorMessage || 'An error occurred during repository ingestion or parsing.'}</p>
            </div>
          </div>
        )}

        {/* Project Header Banner */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-white flex items-center gap-2">
                <FileCode className="w-6 h-6 text-blue-500" />
                {project.repoName || 'Project Details'}
              </h1>
              <p className="text-xs text-slate-400 font-mono mt-1">{project.repoUrl}</p>
            </div>

            <div className="flex items-center gap-6 text-xs text-slate-400 border-t md:border-t-0 md:border-l border-slate-800 pt-4 md:pt-0 md:pl-6">
              <div>
                <span className="block text-slate-500">Nodes Analyzed</span>
                <span className="text-base font-bold text-white mt-0.5 block">{graphNodes.length} Files</span>
              </div>
              <div>
                <span className="block text-slate-500">Created Date</span>
                <span className="text-base font-bold text-white mt-0.5 block">
                  {project.createdAt ? new Date(project.createdAt).toLocaleDateString() : 'N/A'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="flex border-b border-slate-800 bg-slate-900/60 p-1.5 rounded-2xl gap-1">
          <button
            onClick={() => setActiveTab('doc')}
            className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-xs font-semibold rounded-xl transition ${
              activeTab === 'doc'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
            }`}
          >
            <FileText className="w-4 h-4" />
            Documentation View
          </button>
          <button
            onClick={() => setActiveTab('graph')}
            className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-xs font-semibold rounded-xl transition ${
              activeTab === 'graph'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
            }`}
          >
            <Network className="w-4 h-4" />
            Dependency Graph
          </button>
          <button
            onClick={() => setActiveTab('chat')}
            className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-xs font-semibold rounded-xl transition ${
              activeTab === 'chat'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
            }`}
          >
            <MessageSquare className="w-4 h-4" />
            AI Chat Assistant
          </button>
        </div>

        {/* Main Tab Views */}
        {activeTab === 'doc' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* File List Panel */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 h-[550px] flex flex-col">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 px-2 flex items-center gap-2">
                <Layers className="w-4 h-4 text-blue-500" />
                Parsed Project Files ({graphNodes.length})
              </h3>
              <div className="flex-1 overflow-y-auto space-y-1 pr-1">
                {graphNodes.map((node, idx) => (
                  <button
                    key={idx}
                    onClick={() => {
                      setSelectedNode(node);
                      setGeneratedDoc('');
                    }}
                    className={`w-full text-left p-3 rounded-xl transition text-xs flex items-center justify-between ${
                      selectedNode?.path === node.path
                        ? 'bg-blue-600/20 border border-blue-500/40 text-blue-300 font-semibold'
                        : 'hover:bg-slate-800/60 text-slate-300 border border-transparent'
                    }`}
                  >
                    <div className="truncate pr-2">
                      <p className="font-mono text-xs">{node.name}</p>
                      <p className="text-[10px] text-slate-500 truncate">{node.path}</p>
                    </div>
                    <span className="uppercase text-[9px] font-bold px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                      {node.language || 'code'}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {/* Documentation & Code Preview Panel */}
            <div className="lg:col-span-2 space-y-6">
              {selectedNode ? (
                <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 h-[550px] flex flex-col overflow-hidden">
                  <div className="flex items-center justify-between border-b border-slate-800 pb-4 mb-4">
                    <div>
                      <h3 className="text-base font-bold text-white font-mono">{selectedNode.name}</h3>
                      <p className="text-xs text-slate-400">{selectedNode.path}</p>
                    </div>
                    <button
                      onClick={() => handleGenerateDocForNode(selectedNode)}
                      disabled={docLoading}
                      className="flex items-center gap-1.5 text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white px-3.5 py-2 rounded-xl transition shadow-md shadow-blue-600/20 disabled:opacity-50"
                    >
                      {docLoading ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Sparkles className="w-3.5 h-3.5" />}
                      Generate Javadoc
                    </button>
                  </div>

                  <div className="flex-1 overflow-y-auto space-y-4">
                    {/* Generated Documentation Block */}
                    {generatedDoc && (
                      <div className="bg-slate-950 border border-blue-500/30 rounded-xl p-4">
                        <h4 className="text-xs font-bold text-blue-400 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                          <Sparkles className="w-3.5 h-3.5" />
                          AI Generated Documentation
                        </h4>
                        <pre className="text-xs text-emerald-300 font-mono whitespace-pre-wrap">{generatedDoc}</pre>
                      </div>
                    )}

                    {/* Code Snippet Preview */}
                    <div>
                      <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Source Code Preview</h4>
                      <pre className="bg-slate-950 border border-slate-800 rounded-xl p-4 text-xs font-mono text-slate-300 overflow-x-auto max-h-[320px]">
                        {selectedNode.content || '// Empty file content'}
                      </pre>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500">
                  Select a file from the list to view documentation and source code
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'graph' && (
          <div className="space-y-4">
            <GraphView nodes={graphNodes} onSelectNode={(nodeData) => setSelectedNode(nodeData)} />

            {/* Selected Node Details Drawer */}
            {selectedNode && (
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 animate-in slide-in-from-bottom-4 duration-200">
                <div className="flex items-center justify-between mb-4">
                  <h4 className="text-sm font-bold text-white flex items-center gap-2">
                    <Code2 className="w-4 h-4 text-blue-400" />
                    Selected File: <span className="font-mono text-blue-300">{selectedNode.id || selectedNode.path}</span>
                  </h4>
                  <button
                    onClick={() => setSelectedNode(null)}
                    className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
                  <div>
                    <span className="text-slate-500 block mb-1">Language</span>
                    <span className="font-mono bg-slate-950 border border-slate-800 px-2.5 py-1 rounded text-slate-200 uppercase">
                      {selectedNode.language || 'code'}
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-500 block mb-1">Path</span>
                    <span className="font-mono text-slate-300 truncate block">{selectedNode.id || selectedNode.path}</span>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {activeTab === 'chat' && (
          <ChatInterface project={project} selectedFileNode={selectedNode} />
        )}
      </div>
    </div>
  );
};

export default ProjectDetail;
