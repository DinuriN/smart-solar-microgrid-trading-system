import React from 'react'

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import DashboardLayout from './components/layout/DashboardLayout';
import Welcome from './pages/admin/Welcome';
import ProsumersList from './pages/admin/ProsumersList';
import NodesList from './pages/admin/NodesList';
import WebUsersList from './pages/admin/WebUsersList';
import ReservationsList from "./pages/admin/ReservationsList";
import ProtectedRoute from './components/auth/ProtectedRoute';
import Dashboard from './pages/admin/Dashboard';
import { PageHeaderProvider } from './context/PageHeaderContext';


function App() {
  return (
    <BrowserRouter>
      <PageHeaderProvider>
        <Routes>

          {/* Public Routes */}
          <Route path="/login" element={<Login />} />

          {/* Default route redirect to login */}
          <Route path="/" element={<Navigate to="/login" replace />} />

          {/* Protected Routes Wrapper - Thin Client (Only checks if logged in) */}
          <Route element={<ProtectedRoute />}>

            {/* Admin Dashboard Layout */}
            <Route path="/admin" element={<DashboardLayout />}>
              {/* <Route index element={<Navigate to="welcome" replace />} /> */}
              <Route index element={<Navigate to="dashboard" replace />} />
              <Route path="welcome" element={<Welcome />} />
              <Route path="prosumers" element={<ProsumersList />} />
              <Route path="nodes" element={<NodesList />} />
              <Route path="users" element={<WebUsersList />} />
              <Route path="reservations" element={<ReservationsList />} />
              <Route path="dashboard" element={<Dashboard />} />
                
            </Route>

          </Route>

        </Routes>
      </PageHeaderProvider>
    </BrowserRouter>
  );
}

export default App;