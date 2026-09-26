import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';

export default function Sidebar() {


  const navigate = useNavigate();
  const Icons = {
    dashboard: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" className="w-4 h-4 opacity-85 shrink-0">
        <rect x="3" y="3" width="7" height="9" rx="1" /><rect x="14" y="3" width="7" height="5" rx="1" /><rect x="14" y="12" width="7" height="9" rx="1" /><rect x="3" y="16" width="7" height="5" rx="1" />
      </svg>
    ),
    users: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" className="w-4 h-4 opacity-85 shrink-0">
        <circle cx="12" cy="8" r="3.2" /><path d="M4 20c1.5-4 5-6 8-6s6.5 2 8 6" />
      </svg>
    ),
    shield: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" className="w-4 h-4 opacity-85 shrink-0">
        <rect x="3" y="4" width="18" height="16" rx="2" /><path d="M7 9h10M7 13h6" />
      </svg>
    ),
    nodes: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" className="w-4 h-4 opacity-85 shrink-0">
        <circle cx="12" cy="12" r="3" /><circle cx="4" cy="6" r="2" /><circle cx="20" cy="6" r="2" /><circle cx="4" cy="18" r="2" /><circle cx="20" cy="18" r="2" />
        <path d="M6 6.5l4 4M14 13.5l4 4M6 17.5l4-4M14 10.5l4-4" />
      </svg>
    ),
    reservations: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" className="w-4 h-4 opacity-85 shrink-0">
        <rect x="3" y="4" width="18" height="18" rx="2" /><path d="M16 2v4M8 2v4M3 10h18" /><path d="M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01" />
      </svg>
    ),
  };

  const navItemClass = ({ isActive }) =>
    `flex items-center gap-3 px-4 py-2.5 text-sm transition-all border-l-2 ${isActive
      ? 'text-white bg-slate-800/50 border-amber-500'
      : 'text-slate-400 border-transparent hover:text-white hover:bg-slate-800/30'
    }`;


  // FAT Backend: Get user details and dynamic menu from localStorage
  const userName = localStorage.getItem('userName') || 'User';
  const userRole = localStorage.getItem('userRole') || 'Role';
  const initial = userName.charAt(0).toUpperCase();
  const menuItems = JSON.parse(localStorage.getItem('menu') || '[]');


  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    localStorage.removeItem('menu');
    navigate('/login');
  };


  return (
    <aside className="w-60 bg-slate-900 border-r border-slate-800 flex flex-col h-screen sticky top-0 shrink-0">

      {/* Brand */}
      <div className="p-6 pb-8">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-amber-500 rounded flex items-center justify-center">
            <svg viewBox="0 0 24 24" fill="none" className="w-4 h-4 text-slate-950">
              <path d="M13 2L4 14h6l-1 8 9-12h-6l1-8z" fill="currentColor" />
            </svg>
          </div>
          <span className="text-white font-bold tracking-wide">Solara Grid</span>
        </div>
      </div>

      {/* Dynamic Navigation - driven entirely by the backend menu response */}
      <nav className="flex-1 overflow-y-auto pb-4">
        {menuItems.length === 0 ? (
          <div className="px-5 text-[11px] text-slate-500 mt-4">No menu items assigned.</div>
        ) : (
          menuItems.map((item) => (
            <NavLink to={item.path} key={item.label} className={navItemClass}>
              {Icons[item.icon] || Icons.dashboard}
              {item.label}
            </NavLink>
          ))
        )}
      </nav>

      {/* Footer / User Profile */}
      <div className="p-4 border-t border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-xs font-semibold text-teal-400">
            {initial}
          </div>
          <div className="leading-tight truncate">
            <div className="text-sm font-medium text-white truncate">{userName}</div>
            <div className="text-[10px] text-slate-500 font-mono truncate">{userRole}</div>
          </div>

          <button
            onClick={handleLogout}
            title="Log Out"
            className="p-2 text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors ml-auto"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-4 h-4">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
          </button>



        </div>
      </div>

    </aside>
  );
}
