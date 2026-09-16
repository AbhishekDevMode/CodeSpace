function NodeInfoPanel({ node, onClose }) {
    return (
        <div className="absolute z-10 right-4 bottom-4 w-80 bg-slate-950 border border-slate-700 rounded-xl p-4 text-sm shadow-2xl">
            <button className="absolute right-3 top-2 text-slate-400 hover:text-white" onClick={onClose}>×</button>

            <h3>{node.label}</h3>

            <dl className="mt-3 space-y-2 text-xs">
                <dt className="text-slate-500">Path</dt><dd className="text-slate-200 break-all">{node.path}</dd>
                <dt className="text-slate-500">Language</dt><dd className="text-slate-200">{node.language}</dd>
                <dt className="text-slate-500">Type</dt><dd className="text-slate-200 capitalize">{node.type}</dd>
                <dt className="text-slate-500">Relationships</dt><dd className="text-slate-200">{node.weight}</dd>
            </dl>
        </div>
    );
}

export default NodeInfoPanel;
