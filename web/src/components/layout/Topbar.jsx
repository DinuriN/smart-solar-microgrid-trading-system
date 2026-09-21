import React from 'react';

export default function Topbar() {
  return (
    <header className="h-16 border-b border-slate-800 flex items-center justify-between px-8 bg-slate-950/80 backdrop-blur-md sticky top-0 z-10">
      
      <div>
        {/* Pages will render their own big titles below the topbar to match the prototype's layout better */}
      </div>

      <div className="flex items-center">
        <div className="flex items-center gap-2 font-mono text-[11px] text-slate-400">
          {/* Animated pulsing dot */}
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-teal-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-teal-500"></span>
          </span>
          LIVE · System Online
        </div>
      </div>

    </header>
  );
}
