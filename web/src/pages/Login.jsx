import React, { useState } from 'react';
import { authService } from '../api/authService';

export default function Login() {
  const [emailOrNic, setEmailOrNic] = useState('');
  const [password, setPassword] = useState('');
  
  // States for API feedback
  const [errorMsg, setErrorMsg] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // 2. Form Submission Handler
  const handleLogin = async (e) => {
    e.preventDefault(); 
    setErrorMsg(null);
    setIsLoading(true);

    try {
      // Call the C# Backend
      const data = await authService.login(emailOrNic, password);
      
      // If success, save the JWT token
      localStorage.setItem('token', data.token);
      
      // Tell the user it worked!
      alert(`Login Successful! Welcome ${data.user.name}. Role: ${data.user.role}`);
      
    } catch (error) {
      setErrorMsg(error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    // Background container with ambient glowing orbs
    <div className="min-h-screen flex items-center justify-center bg-slate-950 p-4 relative overflow-hidden">
      
      {/* Ambient background glows */}
      <div className="absolute top-[-10%] left-[-10%] w-[40vw] h-[40vw] bg-amber-500/10 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute bottom-[-10%] right-[-10%] w-[40vw] h-[40vw] bg-teal-500/10 rounded-full blur-[120px] pointer-events-none"></div>

      {/* Glassmorphic Login Box */}
      <div className="w-full max-w-md relative z-10">
        <div className="bg-slate-900/40 backdrop-blur-2xl border border-slate-800/60 p-8 sm:p-10 rounded-3xl shadow-[0_0_40px_rgba(0,0,0,0.5)]">
          
          {/* Brand Header */}
          <div className="flex items-center gap-3 mb-10">
            <div className="w-10 h-10 bg-amber-500 rounded-xl flex items-center justify-center shadow-[0_0_20px_rgba(245,165,36,0.3)]">
              {/* Simple SVG Bolt Icon */}
              <svg viewBox="0 0 24 24" fill="none" className="w-5 h-5 text-slate-950">
                <path d="M13 2L4 14h6l-1 8 9-12h-6l1-8z" fill="currentColor"/>
              </svg>
            </div>
            <div>
              <div className="text-white font-bold tracking-wide text-lg">Solara Grid</div>
            </div>
          </div>

          {/* Title */}
          <h1 className="text-2xl font-semibold text-white mb-3">Sign in</h1>
          <p className="text-slate-400 text-sm mb-8 leading-relaxed">
            Manage microgrid nodes, prosumer accounts and energy slot reservations.
          </p>

          {/* Login Form */}
          <form onSubmit={handleLogin} className="space-y-5">
            
            {/* Error Message Display */}
            {errorMsg && (
              <div className="bg-red-500/10 border border-red-500/50 text-red-400 text-xs px-4 py-3 rounded-xl">
                {errorMsg}
              </div>
            )}

            {/* Username Field */}
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-2">Username (Email or NIC)</label>
              <input
                type="text"
                value={emailOrNic}
                onChange={(e) => setEmailOrNic(e.target.value)}
                disabled={isLoading}
                className="w-full bg-slate-950/50 border border-slate-800/80 text-white px-4 py-3 rounded-3xl text-sm outline-none focus:border-amber-500/80 focus:ring-1 focus:ring-amber-500/50 transition-all placeholder-slate-600 disabled:opacity-50"
                placeholder="e.g. 199912345678"
                required
              />
            </div>

            {/* Password Field */}
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-2">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isLoading}
                className="w-full bg-slate-950/50 border border-slate-800/80 text-white px-4 py-3 rounded-3xl text-sm outline-none focus:border-amber-500/80 focus:ring-1 focus:ring-amber-500/50 transition-all placeholder-slate-600 disabled:opacity-50"
                placeholder="••••••••"
                required
              />
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex justify-center items-center bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold py-3.5 rounded-3xl text-sm transition-all mt-4 shadow-[0_0_20px_rgba(245,165,36,0.2)] hover:shadow-[0_0_25px_rgba(245,165,36,0.4)] hover:-translate-y-0.5 active:translate-y-0 disabled:opacity-70 disabled:hover:translate-y-0"
            >
              {isLoading ? (
                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-slate-950" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              ) : null}
              {isLoading ? 'Signing in...' : 'Sign in to workspace'}
            </button>

          </form>

        </div>
      </div>
    </div>
  );
}
