import React, { useEffect } from 'react';
import { usePageHeader } from '../../context/PageHeaderContext';

export default function OperatorDashboard() {
  const { setHeader } = usePageHeader();

  useEffect(() => {
    setHeader({
      title: "Grid Operations Center",
      breadcrumb: "solara-grid / operator / dashboard"
    });
  }, []);
  return (
    <div className="flex items-center justify-center h-full text-slate-400">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-white mb-2">Grid Operator Dashboard</h2>
        <p>Welcome to the grid operations control center.</p>
        <p className="text-sm mt-4 text-amber-500">More widgets coming soon.</p>
      </div>
    </div>
  );
}
