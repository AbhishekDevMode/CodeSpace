function GraphLegend() {
    const items = [
        { color: '#2563eb', label: 'Packages' },
        { color: '#059669', label: 'Classes / files' },
        { color: '#7c3aed', label: 'Methods' }
    ];

    return (
        <div className="absolute left-4 bottom-4 bg-slate-950/95 border border-slate-700 rounded-xl p-3 text-xs text-slate-300 shadow-xl">
            <h4 className="font-semibold text-white mb-2">Node levels</h4>
            {items.map(item => (
                <div key={item.label} className="flex items-center gap-2 mb-1">
                    <span
                        className="w-2.5 h-2.5 rounded-full"
                        style={{ backgroundColor: item.color }}
                    />
                    <span>{item.label}</span>
                </div>
            ))}
        </div>
    );
}

export default GraphLegend;
