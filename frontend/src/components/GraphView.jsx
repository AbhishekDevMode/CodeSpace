import React, { useRef } from 'react';
import CytoscapeComponent from 'react-cytoscapejs';
import { ZoomIn, ZoomOut, Maximize2, RefreshCw } from 'lucide-react';

const GraphView = ({ nodes = [], onSelectNode }) => {
  const cyRef = useRef(null);

  // Transform backend FileNode data to Cytoscape elements (nodes & edges)
  const elements = [];
  const nodeSet = new Set();

  // Add Nodes
  nodes.forEach((file) => {
    if (!file.path) return;
    nodeSet.add(file.path);
    elements.push({
      data: {
        id: file.path,
        label: file.name || file.path,
        language: file.language || 'code',
        content: file.content || '',
      },
    });
  });

  // Add Edges (IMPORTS & CALLS)
  nodes.forEach((file) => {
    const srcId = file.path;

    if (file.imports && Array.isArray(file.imports)) {
      file.imports.forEach((target) => {
        const targetId = typeof target === 'string' ? target : target.path;
        if (targetId && nodeSet.has(targetId)) {
          elements.push({
            data: {
              id: `${srcId}-imports-${targetId}`,
              source: srcId,
              target: targetId,
              label: 'IMPORTS',
              type: 'imports',
            },
          });
        }
      });
    }

    if (file.calls && Array.isArray(file.calls)) {
      file.calls.forEach((target) => {
        const targetId = typeof target === 'string' ? target : target.path;
        if (targetId && nodeSet.has(targetId)) {
          elements.push({
            data: {
              id: `${srcId}-calls-${targetId}`,
              source: srcId,
              target: targetId,
              label: 'CALLS',
              type: 'calls',
            },
          });
        }
      });
    }
  });

  // Cytoscape Layout
  const layout = {
    name: 'cose',
    animate: true,
    animationDuration: 500,
    padding: 30,
    nodeRepulsion: 8000,
    idealEdgeLength: 100,
  };

  // Cytoscape Stylesheet
  const style = [
    {
      selector: 'node',
      style: {
        label: 'data(label)',
        'background-color': '#3b82f6',
        color: '#f8fafc',
        'font-size': '11px',
        'text-valign': 'center',
        'text-halign': 'center',
        width: '75px',
        height: '75px',
        'border-width': '2px',
        'border-color': '#60a5fa',
        'overlay-padding': '6px',
        'text-outline-color': '#0f172a',
        'text-outline-width': '2px',
      },
    },
    {
      selector: 'node[language = "java"]',
      style: {
        'background-color': '#ea580c',
        'border-color': '#f97316',
      },
    },
    {
      selector: 'node[language = "python"]',
      style: {
        'background-color': '#0284c7',
        'border-color': '#38bdf8',
      },
    },
    {
      selector: 'node[language = "javascript"]',
      style: {
        'background-color': '#ca8a04',
        'border-color': '#facc15',
      },
    },
    {
      selector: 'node:selected',
      style: {
        'border-width': '4px',
        'border-color': '#ffffff',
        'background-color': '#2563eb',
      },
    },
    {
      selector: 'edge',
      style: {
        width: 2,
        'line-color': '#475569',
        'target-arrow-color': '#475569',
        'target-arrow-shape': 'triangle',
        'curve-style': 'bezier',
        label: 'data(label)',
        'font-size': '8px',
        color: '#94a3b8',
        'text-rotation': 'autorotate',
      },
    },
    {
      selector: 'edge[type = "calls"]',
      style: {
        'line-color': '#a855f7',
        'target-arrow-color': '#a855f7',
        'line-style': 'dashed',
      },
    },
  ];

  const handleZoomIn = () => {
    if (cyRef.current) {
      cyRef.current.zoom(cyRef.current.zoom() * 1.2);
    }
  };

  const handleZoomOut = () => {
    if (cyRef.current) {
      cyRef.current.zoom(cyRef.current.zoom() * 0.8);
    }
  };

  const handleFit = () => {
    if (cyRef.current) {
      cyRef.current.fit();
    }
  };

  const handleResetLayout = () => {
    if (cyRef.current) {
      cyRef.current.layout(layout).run();
    }
  };

  return (
    <div className="relative w-full h-[550px] bg-slate-950 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
      {/* Zoom / Pan Controls Toolbar */}
      <div className="absolute top-4 right-4 z-10 flex items-center gap-1.5 bg-slate-900/90 border border-slate-800 backdrop-blur-md p-1.5 rounded-xl shadow-lg">
        <button
          onClick={handleZoomIn}
          className="p-1.5 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg transition"
          title="Zoom In"
        >
          <ZoomIn className="w-4 h-4" />
        </button>
        <button
          onClick={handleZoomOut}
          className="p-1.5 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg transition"
          title="Zoom Out"
        >
          <ZoomOut className="w-4 h-4" />
        </button>
        <button
          onClick={handleFit}
          className="p-1.5 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg transition"
          title="Fit to Canvas"
        >
          <Maximize2 className="w-4 h-4" />
        </button>
        <button
          onClick={handleResetLayout}
          className="p-1.5 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg transition"
          title="Reset Layout"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Legend */}
      <div className="absolute bottom-4 left-4 z-10 flex items-center gap-3 bg-slate-900/90 border border-slate-800 backdrop-blur-md px-3 py-2 rounded-xl text-xs text-slate-300 shadow-lg">
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-orange-500"></span>
          <span>Java</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-sky-500"></span>
          <span>Python</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-yellow-500"></span>
          <span>JavaScript</span>
        </div>
      </div>

      {elements.length === 0 ? (
        <div className="flex flex-col items-center justify-center h-full text-slate-500 text-sm">
          <p>No dependency nodes available for graph rendering</p>
        </div>
      ) : (
        <CytoscapeComponent
          elements={elements}
          style={{ width: '100%', height: '100%' }}
          layout={layout}
          stylesheet={style}
          cy={(cy) => {
            cyRef.current = cy;
            cy.off('tap', 'node');
            cy.on('tap', 'node', (evt) => {
              const nodeData = evt.target.data();
              if (onSelectNode) {
                onSelectNode(nodeData);
              }
            });
          }}
        />
      )}
    </div>
  );
};

export default GraphView;
