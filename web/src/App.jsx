import React from 'react'

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import DashboardLayout from './components/layout/DashboardLayout';
import ProsumersList from './pages/admin/ProsumersList';
import ProtectedRoute from './components/auth/ProtectedRoute';


function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* Public Routes */}
        <Route path="/login" element={<Login />} />

        {/* Default route redirect to login */}
        <Route path="/" element={<Navigate to="/login" replace />} />

        {/* Protected Routes Wrapper - Thin Client (Only checks if logged in) */}
        <Route element={<ProtectedRoute />}>

          {/* Admin Dashboard Layout */}
          <Route path="/admin" element={<DashboardLayout />}>
            <Route index element={<Navigate to="prosumers" replace />} />
            <Route path="prosumers" element={<ProsumersList />} />
            <Route path="users" element={<div className="p-8 text-white">Web Users screen coming soon...</div>} />
          </Route>

        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;