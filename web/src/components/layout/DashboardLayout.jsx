import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Topbar from './Topbar';

export default function DashboardLayout() {
  return (
    <div className="flex min-h-screen bg-slate-950 text-slate-300 font-sans">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar />
        <main className="flex-1 overflow-auto p-8">
          {/* Outlet is where the specific page content (like ProsumersList) will be rendered */}
          <Outlet />
        </main>
      </div>
    </div>
  );
}
