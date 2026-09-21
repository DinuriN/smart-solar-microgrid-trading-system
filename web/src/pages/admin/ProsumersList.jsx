import React, { useEffect, useState } from 'react';
import { prosumerService } from '../../api/prosumerService';

export default function ProsumersList() {
  const [prosumers, setProsumers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProsumers();
  }, []);

  const fetchProsumers = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await prosumerService.getAll();
      setProsumers(data);
    } catch (err) {
      setError(err.message || "Failed to load prosumers from server.");
      setProsumers([]); // Strictly thin client: If API fails, show nothing.
    } finally {
      setIsLoading(false);
    }
  };

  const handleStatusToggle = async (nic, action) => {
    try {
      if (action === 'deactivate') {
        await prosumerService.deactivate(nic);
      } else if (action === 'reactivate') {
        await prosumerService.reactivate(nic);
      }
      fetchProsumers(); // Refresh data from backend
    } catch (err) {
      alert(err.response?.data?.message || `Failed to ${action} prosumer`);
    }
  };

  return (
    <div className="max-w-7xl">
      
      <div className="mb-8">
        <h1 className="text-xl font-semibold text-white">Prosumer Directory</h1>
        <div className="text-slate-500 font-mono text-xs mt-1">solara-grid / administration / prosumers</div>
      </div>

      {error && (
        <div className="bg-red-500/10 border border-red-500 text-red-500 px-4 py-3 rounded mb-5 text-sm">
          {error}
        </div>
      )}

      {/* Toolbar simplified to only what backend supports (Create) */}
      <div className="flex items-center justify-end mb-5">
        <button className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold px-4 py-2 rounded-lg text-sm transition-colors">
          + Register Prosumer
        </button>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-lg">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">NIC</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Name</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Email</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Phone</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Address</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Status</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Actions</th>
            </tr>
          </thead>
          <tbody>
            {(prosumers || []).map((p, i) => (
              <tr key={i} className="hover:bg-slate-800/50 transition-colors group">
                <td className="px-5 py-3 border-b border-slate-800 font-mono text-xs text-slate-400">{p.nic}</td>
                <td className="px-5 py-3 border-b border-slate-800 text-sm text-slate-200">{p.name}</td>
                <td className="px-5 py-3 border-b border-slate-800 text-xs text-slate-400">{p.email}</td>
                <td className="px-5 py-3 border-b border-slate-800 font-mono text-xs text-slate-400">{p.phone}</td>
                <td className="px-5 py-3 border-b border-slate-800 text-xs text-slate-400 truncate max-w-[150px]">{p.address}</td>
                
                <td className="px-5 py-3 border-b border-slate-800">
                  <div className="flex flex-col gap-1 items-start">
                    {p.isActive 
                      ? <span className="bg-teal-500/10 text-teal-400 font-mono text-[9px] px-2 py-0.5 rounded-full">ACTIVE</span> 
                      : <span className="bg-slate-500/10 text-slate-400 font-mono text-[9px] px-2 py-0.5 rounded-full">INACTIVE</span>}
                    {p.deactivationRequested && (
                       <span className="bg-amber-500/10 text-amber-500 font-mono text-[9px] px-2 py-0.5 rounded-full">REQUESTED DEL</span>
                    )}
                  </div>
                </td>
                
                <td className="px-5 py-3 border-b border-slate-800 text-right">
                  <div className="flex justify-end gap-2">
                    {p.deactivationRequested && p.isActive && (
                      <>
                        <button 
                          onClick={() => handleStatusToggle(p.nic, 'reactivate')}
                          className="px-2 py-1 border border-slate-600 text-slate-400 rounded text-[10px] hover:bg-slate-800 hover:text-white"
                        >
                          Reject
                        </button>
                        <button 
                          onClick={() => handleStatusToggle(p.nic, 'deactivate')}
                          className="px-2 py-1 border border-amber-500/30 text-amber-500 rounded text-[10px] hover:bg-amber-500/10"
                        >
                          Approve
                        </button>
                      </>
                    )}
                    {!p.isActive && (
                      <button 
                        onClick={() => handleStatusToggle(p.nic, 'reactivate')}
                        className="px-2 py-1 border border-amber-500/30 text-amber-500 rounded text-[10px] hover:bg-amber-500/10"
                      >
                        Reactivate
                      </button>
                    )}
                    {p.isActive && !p.deactivationRequested && (
                      <button 
                        onClick={() => handleStatusToggle(p.nic, 'deactivate')}
                        className="px-2 py-1 border border-red-500/30 text-red-400 rounded text-[10px] hover:bg-red-500/10"
                      >
                        Deactivate
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {!(prosumers && prosumers.length > 0) && !isLoading && (
              <tr>
                <td colSpan="7" className="px-5 py-8 text-center text-slate-500 text-sm">
                  No prosumers found in the database.
                </td>
              </tr>
            )}
          </tbody>
        </table>
        
        {isLoading && (
          <div className="p-8 text-center text-slate-500 text-sm flex items-center justify-center gap-2">
            <svg className="animate-spin h-4 w-4 text-amber-500" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Fetching from server...
          </div>
        )}
      </div>

    </div>
  );
}
