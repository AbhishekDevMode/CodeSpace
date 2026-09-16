import { useCallback, useEffect, useMemo, useState } from 'react';
import CytoscapeComponent from 'react-cytoscapejs';
import { graphService } from '../../api/graphService';
import GraphControls from './GraphControls';
import NodeInfoPanel from './NodeInfoPanel';
import GraphLegend from './GraphLegend';

function DependencyGraph({ projectId }) {
  const [elements, setElements] = useState([]);
  const [depth, setDepth] = useState(1);
  const [loading, setLoading] = useState(true);
  const [selectedNode, setSelectedNode] = useState(null);
  const [error, setError] = useState('');

  const loadGraph = useCallback(async (nextDepth, nextFocus) => {
    setLoading(true);
    setError('');
    setSelectedNode(null);
    try {
      const data = await graphService.getGraph(projectId, nextDepth, nextFocus || null);
      setElements(toCytoscapeElements(data));
    } catch (requestError) {
      setElements([]);
      setError(requestError.response?.data?.message || 'Unable to load the dependency graph.');
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    void Promise.resolve().then(() => loadGraph(depth, ''));
  }, [projectId, depth, loadGraph]);

  const handleSearch = (searchTerm) => {
    const nextFocus = searchTerm.trim();
    loadGraph(2, nextFocus);
  };

  const stylesheet = useMemo(() => [
    { selector: 'node[type = "package"]', style: { 'background-color': '#2563eb', shape: 'round-rectangle' } },
    { selector: 'node[type = "class"]', style: { 'background-color': '#059669', shape: 'ellipse' } },
    { selector: 'node[type = "method"]', style: { 'background-color': '#7c3aed', shape: 'round-rectangle' } },
    { selector: 'node', style: {
      label: 'data(label)', width: 'mapData(weight, 0, 20, 36, 72)', height: 'mapData(weight, 0, 20, 36, 72)',
      'font-size': '10px', color: '#e2e8f0', 'text-wrap': 'wrap', 'text-max-width': '100px',
      'text-valign': 'center', 'text-halign': 'center', 'border-width': 2, 'border-color': '#cbd5e1'
    } },
    { selector: 'edge', style: {
      width: 'mapData(weight, 1, 10, 1, 4)', 'line-color': '#64748b', 'target-arrow-color': '#64748b',
      'target-arrow-shape': 'triangle', 'curve-style': 'bezier', 'arrow-scale': 0.8
    } },
    { selector: ':selected', style: { 'border-width': 4, 'border-color': '#fbbf24' } }
  ], []);

  return (
    <section className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden min-h-[620px]">
      <GraphControls
        depth={depth}
        onDepthChange={setDepth}
        onSearch={handleSearch}
        onReset={() => loadGraph(depth, '')}
      />
      <div className="relative h-[540px]">
        {loading ? <div className="h-full grid place-items-center text-sm text-slate-400">Loading graph…</div>
          : error ? <div className="h-full grid place-items-center text-sm text-red-400">{error}</div>
          : elements.length === 0 ? <div className="h-full grid place-items-center text-sm text-slate-400">No matching code relationships found.</div>
          : <CytoscapeComponent
            elements={elements}
            stylesheet={stylesheet}
            style={{ width: '100%', height: '100%' }}
            layout={{ name: 'cose', padding: 36, idealEdgeLength: 130, nodeRepulsion: 9000, animate: false }}
            cy={(cy) => {
              cy.off('tap', 'node');
              cy.on('tap', 'node', (event) => setSelectedNode(event.target.data()));
            }}
          />}
        <GraphLegend />
        {selectedNode && <NodeInfoPanel node={selectedNode} onClose={() => setSelectedNode(null)} />}
      </div>
    </section>
  );
}

function toCytoscapeElements(data = {}) {
  const nodes = (data.nodes || []).map((node) => ({ data: node }));
  const edges = (data.edges || []).map((edge) => ({ data: { ...edge, id: `edge:${edge.source}->${edge.target}` } }));
  return [...nodes, ...edges];
}

export default DependencyGraph;
