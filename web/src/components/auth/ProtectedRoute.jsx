import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

export default function ProtectedRoute() {
  const token = localStorage.getItem('token');

  // 1. Thin Client logic: Only check if they are logged in.
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // 2. We do NOT check roles here! The FAT Backend will enforce authorization.
  // If they access a page they shouldn't, the API will return 403 Forbidden.
  return <Outlet />;
}
