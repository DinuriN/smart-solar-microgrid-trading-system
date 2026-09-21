import React, { useEffect, useState } from 'react';
import { webUserService } from '../../api/webUserService';

export default function WebUsersList() {
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [modalError, setModalError] = useState(null);
  const [formData, setFormData] = useState({
    name: '', email: '', phone: '', password: '', role: 'GridOperator'
  });


  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await webUserService.getAll();
      setUsers(data);
    } catch (err) {
      setError(err.message || "Failed to load web users from server.");
      setUsers([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleResetPassword = async (id, name) => {
    const newPassword = window.prompt(`Enter new password for ${name}:`);
    if (!newPassword) return; // Cancelled

    try {
      await webUserService.resetPassword(id, newPassword);
      alert(`Password for ${name} has been reset successfully!`);
    } catch (err) {
      alert(err.response?.data?.message || "Failed to reset password.");
    }
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setModalError(null);
    try {
      await webUserService.create(formData);
      setIsModalOpen(false);
      setFormData({ name: '', email: '', phone: '', password: '', role: 'GridOperator' });
      fetchUsers(); // Refresh table
    } catch (err) {
      setModalError(err.response?.data?.message || err.message || "Failed to create user.");
    } finally {
      setIsSubmitting(false);
    }
  };






  // Maps roles nicely. With JsonStringEnumConverter, backend sends "GridOperator" or "BackOfficeUser"
  const getRoleBadge = (role) => {
    if (role === 'GridOperator') {
      return <span className="bg-blue-500/10 text-blue-400 font-mono text-[10px] px-2.5 py-1 rounded-full">GRID OPERATOR</span>;
    }
    if (role === 'BackOfficeUser') {
      return <span className="bg-purple-500/10 text-purple-400 font-mono text-[10px] px-2.5 py-1 rounded-full">BACK OFFICE</span>;
    }
    return <span className="bg-slate-500/10 text-slate-400 font-mono text-[10px] px-2.5 py-1 rounded-full">{role}</span>;
  };

  return (
    <div className="max-w-7xl">

      <div className="mb-8">
        <h1 className="text-xl font-semibold text-white">Web Users Directory</h1>
        <div className="text-slate-500 font-mono text-xs mt-1">solara-grid / administration / web users</div>
      </div>

      {error && (
        <div className="bg-red-500/10 border border-red-500 text-red-500 px-4 py-3 rounded mb-5 text-sm">
          {error}
        </div>
      )}

      {/* Toolbar */}
      <div className="flex items-center justify-end mb-5">
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold px-4 py-2 rounded-lg text-sm transition-colors">
          + Register Web User
        </button>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-lg">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">ID</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Name</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Email</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Phone</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Role</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Status</th>
              <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Actions</th>
            </tr>
          </thead>
          <tbody>
            {(users || []).map((u, i) => (
              <tr key={i} className="hover:bg-slate-800/50 transition-colors group">
                {/* IDs in Mongo are 24 chars, we'll truncate it visually */}
                <td className="px-5 py-3 border-b border-slate-800 font-mono text-[10px] text-slate-500" title={u.id}>
                  ...{u.id.substring(u.id.length - 6)}
                </td>
                <td className="px-5 py-3 border-b border-slate-800 text-sm text-slate-200">{u.name}</td>
                <td className="px-5 py-3 border-b border-slate-800 text-xs text-slate-400">{u.email}</td>
                <td className="px-5 py-3 border-b border-slate-800 font-mono text-xs text-slate-400">{u.phone}</td>
                <td className="px-5 py-3 border-b border-slate-800">
                  {getRoleBadge(u.role)}
                </td>
                <td className="px-5 py-3 border-b border-slate-800">
                  {u.isActive
                    ? <span className="bg-teal-500/10 text-teal-400 font-mono text-[9px] px-2 py-0.5 rounded-full">ACTIVE</span>
                    : <span className="bg-slate-500/10 text-slate-400 font-mono text-[9px] px-2 py-0.5 rounded-full">INACTIVE</span>}
                </td>
                <td className="px-5 py-3 border-b border-slate-800 text-right">
                  <div className="flex justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => handleResetPassword(u.id, u.name)}
                      className="px-2 py-1 border border-amber-500/30 text-amber-500 rounded text-[10px] hover:bg-amber-500/10"
                    >
                      Reset Password
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {!(users && users.length > 0) && !isLoading && (
              <tr>
                <td colSpan="7" className="px-5 py-8 text-center text-slate-500 text-sm">
                  No web users found in the database.
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

      {/* Minimal Glassmorphic Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-700 w-full max-w-md rounded-2xl shadow-2xl p-6 relative">
            <h2 className="text-xl font-semibold text-white mb-4">Register Web User</h2>

            {modalError && <div className="mb-4 text-xs bg-red-500/10 text-red-500 p-2 rounded border border-red-500/20">{modalError}</div>}

            <form onSubmit={handleCreateSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Name</label>
                <input required type="text" value={formData.name} onChange={e => setFormData({ ...formData, name: e.target.value })} className="w-full bg-slate-800/50 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:border-amber-500 focus:outline-none transition-colors" />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Email</label>
                <input required type="email" value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} className="w-full bg-slate-800/50 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:border-amber-500 focus:outline-none transition-colors" />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Phone</label>
                <input required type="text" value={formData.phone} onChange={e => setFormData({ ...formData, phone: e.target.value })} className="w-full bg-slate-800/50 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:border-amber-500 focus:outline-none transition-colors" />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Password</label>
                <input required type="password" value={formData.password} onChange={e => setFormData({ ...formData, password: e.target.value })} className="w-full bg-slate-800/50 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:border-amber-500 focus:outline-none transition-colors" />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Role</label>
                <select value={formData.role} onChange={e => setFormData({ ...formData, role: e.target.value })} className="w-full bg-slate-800/50 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:border-amber-500 focus:outline-none transition-colors">
                  <option value="GridOperator">Grid Operator</option>
                  <option value="BackOfficeUser">Back Office User</option>
                </select>
              </div>

              <div className="pt-4 flex justify-end gap-3 border-t border-slate-800">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-sm text-slate-400 hover:text-white transition-colors">Cancel</button>
                <button type="submit" disabled={isSubmitting} className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold px-5 py-2 rounded-lg text-sm transition-colors disabled:opacity-50">
                  {isSubmitting ? 'Creating...' : 'Register'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}



    </div>
  );
}
