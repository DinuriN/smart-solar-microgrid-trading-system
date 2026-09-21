import React from 'react';

export default function Welcome() {
  return (
    <div className="max-w-7xl h-[60vh] flex flex-col items-center justify-center text-center">
      <div className="w-16 h-16 bg-amber-500/10 rounded-2xl flex items-center justify-center mb-6 border border-amber-500/20 shadow-[0_0_30px_rgba(245,165,36,0.1)]">
        <svg viewBox="0 0 24 24" fill="none" className="w-8 h-8 text-amber-500">
          <path d="M13 2L4 14h6l-1 8 9-12h-6l1-8z" fill="currentColor" />
        </svg>
      </div>

      <h1 className="text-3xl font-bold text-white mb-3">Welcome to Solara Grid</h1>
      <p className="text-slate-400 max-w-md mx-auto">
        Your dashboard is ready. Please select an option from the sidebar to begin managing the grid.
        If your sidebar is empty, you do not have permissions assigned yet.
      </p>
    </div>
  );
}
