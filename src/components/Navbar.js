import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

function Navbar({ user, onLogout }) {
  const navigate = useNavigate();
  const [showProfile, setShowProfile] = useState(false);

  const userRoles = user?.roles || [];
  const isAdmin = userRoles.includes('ROLE_ADMIN');
  const isAnalyst = userRoles.includes('ROLE_ANALYST');
  const isViewer = userRoles.includes('ROLE_VIEWER') || (!isAdmin && !isAnalyst);

  // Only Analyst and Admin can see Records link
  const canViewRecords = isAnalyst || isAdmin;
  // Only Admin can see Users link
  const canManageUsers = isAdmin;

  const getRoleName = () => {
    if (isAdmin) return 'ADMIN';
    if (isAnalyst) return 'ANALYST';
    return 'VIEWER';
  };

  const getRoleIcon = () => {
    if (isAdmin) return '👑';
    if (isAnalyst) return '📊';
    return '👁️';
  };

  const getRoleColor = () => {
    if (isAdmin) return '#ffd700';
    if (isAnalyst) return '#10b981';
    return '#667eea';
  };

  const getPermissions = () => {
    if (isAdmin) return 'Full Access: Create, Edit, Delete Records & Manage Users';
    if (isAnalyst) return 'View Only: Records & Insights';
    return 'View Only: Dashboard';
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    if (onLogout) {
      onLogout();
    }
    navigate('/login');
  };

  if (!user) {
    return null;
  }

  return (
    <nav className="navbar">
      <div className="nav-container">
        <Link to="/dashboard" className="nav-brand">
          💰 Finance Dashboard
        </Link>

        <div className="nav-menu">
          <Link to="/dashboard" className="nav-link">📊 Dashboard</Link>

          {/* Only Analyst and Admin can see Records link */}
          {canViewRecords && (
            <Link to="/records" className="nav-link">📝 Records</Link>
          )}

          {/* Only Admin can see Users link */}
          {canManageUsers && (
            <Link to="/users" className="nav-link">👥 Users</Link>
          )}

          <div className="profile-section">
            <div
              className="profile-trigger"
              onClick={() => setShowProfile(!showProfile)}
            >
              <div className="profile-avatar" style={{ backgroundColor: getRoleColor() }}>
                {getRoleIcon()}
              </div>
              <div className="profile-info">
                <span className="profile-name">{user.username}</span>
                <span className="profile-role" style={{ color: getRoleColor() }}>
                  {getRoleName()}
                </span>
              </div>
              <span className="dropdown-arrow">{showProfile ? '▲' : '▼'}</span>
            </div>

            {showProfile && (
              <div className="profile-dropdown">
                <div className="dropdown-header" style={{ background: `linear-gradient(135deg, ${getRoleColor()} 0%, ${getRoleColor()}dd 100%)` }}>
                  <div className="dropdown-avatar" style={{ backgroundColor: 'rgba(255,255,255,0.2)' }}>
                    {getRoleIcon()}
                  </div>
                  <div className="dropdown-info">
                    <h4>{user.username}</h4>
                    <p>{user.email}</p>
                    <span className="dropdown-role">{getRoleName()}</span>
                  </div>
                </div>
                <div className="dropdown-divider"></div>
                <div className="dropdown-permissions">
                  <div className="permission-title">🔒 YOUR PERMISSIONS</div>
                  <div className="permission-text">{getPermissions()}</div>
                </div>
                <div className="dropdown-divider"></div>
                <div className="dropdown-stats">
                  <div className="stat">
                    <span>Role</span>
                    <strong>{getRoleName()}</strong>
                  </div>
                  <div className="stat">
                    <span>ID</span>
                    <strong>{user.id}</strong>
                  </div>
                </div>
                <div className="dropdown-divider"></div>
                <button onClick={handleLogout} className="logout-dropdown-btn">
                  🚪 Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;