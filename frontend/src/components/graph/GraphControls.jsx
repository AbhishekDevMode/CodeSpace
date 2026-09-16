import { useState } from 'react';

function GraphControls({ depth, onDepthChange, onSearch, onReset }) {
    const [searchTerm, setSearchTerm] = useState('');

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            onSearch(searchTerm);
        }
    };

    return (
        <div className="p-4 border-b border-slate-800 flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
            <div className="flex gap-2">
                <button
                    className={`px-3 py-2 rounded-lg text-xs font-semibold ${depth === 1 ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-300'}`}
                    onClick={() => onDepthChange(1)}
                >
                    Packages
                </button>
                <button
                    className={`px-3 py-2 rounded-lg text-xs font-semibold ${depth === 2 ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-300'}`}
                    onClick={() => onDepthChange(2)}
                >
                    Classes
                </button>
                <button
                    className={`px-3 py-2 rounded-lg text-xs font-semibold ${depth === 3 ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-300'}`}
                    onClick={() => onDepthChange(3)}
                >
                    Methods
                </button>
            </div>

            <div className="flex gap-2">
                <input
                    type="text"
                    placeholder="Search for a class..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white"
                />
                <button className="px-3 py-2 rounded-lg bg-slate-800 text-xs text-slate-200" onClick={() => onSearch(searchTerm)}>Search</button>
                <button className="px-3 py-2 rounded-lg bg-slate-800 text-xs text-slate-200" onClick={() => { setSearchTerm(''); onReset(); }}>
                    Reset
                </button>
            </div>
        </div>
    );
}

export default GraphControls;
