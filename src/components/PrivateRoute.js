import React from 'react';
import { Navigate } from 'react-router-dom';

function PrivateRoute({ children, requiredRoles = [] }) {
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  // Not authenticated
  if (!token) {
    return <Navigate to="/login" />;
  }

  // No specific roles required - just need to be authenticated
  if (requiredRoles.length === 0) {
    return children;
  }

  // Check if user has any of the required roles
  const userRoles = user.roles || [];
  const hasRequiredRole = requiredRoles.some(role =>
    userRoles.includes(role)
  );

  // Doesn't have required role - redirect to dashboard
  if (!hasRequiredRole) {
    return <Navigate to="/dashboard" />;
  }

  return children;
}

export default PrivateRoute;