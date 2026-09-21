import React from 'react'

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import DashboardLayout from './components/layout/DashboardLayout';
import Welcome from './pages/admin/Welcome';
import ProsumersList from './pages/admin/ProsumersList';
import WebUsersList from './pages/admin/WebUsersList';
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
            <Route index element={<Navigate to="welcome" replace />} />
            <Route path="welcome" element={<Welcome />} />
            <Route path="prosumers" element={<ProsumersList />} />
            <Route path="users" element={<WebUsersList />} />
          </Route>

        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;