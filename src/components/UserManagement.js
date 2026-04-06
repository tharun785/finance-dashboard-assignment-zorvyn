import React, { useState, useEffect } from 'react';
import { getAllUsers, updateUserStatus, updateUserRoles, deleteUser } from '../services/api';
import './UserManagement.css';

function UserManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateUser, setShowCreateUser] = useState(false);
  const [newUser, setNewUser] = useState({
    username: '',
    email: '',
    password: '',
    roles: ['viewer']
  });
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await getAllUsers();
      setUsers(response.data);
    } catch (error) {
      console.error('Error fetching users:', error);
      showMessage('error', 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 3000);
  };

  const handleStatusToggle = async (userId, currentStatus) => {
    try {
      await updateUserStatus(userId, !currentStatus);
      showMessage('success', `User ${!currentStatus ? 'activated' : 'deactivated'} successfully`);
      fetchUsers();
    } catch (error) {
      console.error('Error updating user status:', error);
      showMessage('error', 'Failed to update user status');
    }
  };

  const handleRoleChange = async (userId, newRole) => {
    try {
      await updateUserRoles(userId, [newRole]);
      showMessage('success', 'User role updated successfully');
      fetchUsers();
    } catch (error) {
      console.error('Error updating user roles:', error);
      showMessage('error', 'Failed to update user role');
    }
  };

  const handleDeleteUser = async (userId) => {
    if (window.confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      try {
        await deleteUser(userId);
        showMessage('success', 'User deleted successfully');
        fetchUsers();
      } catch (error) {
        console.error('Error deleting user:', error);
        showMessage('error', 'Failed to delete user');
      }
    }
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('http://localhost:8080/api/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(newUser)
      });

      if (response.ok) {
        setShowCreateUser(false);
        setNewUser({ username: '', email: '', password: '', roles: ['viewer'] });
        showMessage('success', 'User created successfully');
        fetchUsers();
      } else {
        const error = await response.json();
        showMessage('error', error.error || 'Failed to create user');
      }
    } catch (error) {
      console.error('Error creating user:', error);
      showMessage('error', 'Failed to create user');
    }
  };

  const getRoleBadgeClass = (role) => {
    if (role === 'ROLE_ADMIN') return 'role-admin';
    if (role === 'ROLE_ANALYST') return 'role-analyst';
    return 'role-viewer';
  };

  const getRoleDisplayName = (role) => {
    if (role === 'ROLE_ADMIN') return 'Admin';
    if (role === 'ROLE_ANALYST') return 'Analyst';
    return 'Viewer';
  };

  const getRoleIcon = (role) => {
    if (role === 'ROLE_ADMIN') return '👑';
    if (role === 'ROLE_ANALYST') return '📊';
    return '👁️';
  };

  const getTotalUsersByRole = () => {
    const counts = { admin: 0, analyst: 0, viewer: 0 };
    users.forEach(user => {
      const role = user.roles[0]?.name;
      if (role === 'ROLE_ADMIN') counts.admin++;
      else if (role === 'ROLE_ANALYST') counts.analyst++;
      else counts.viewer++;
    });
    return counts;
  };

  const roleCounts = getTotalUsersByRole();

  if (loading) return <div className="loading">Loading users...</div>;

  return (
    <div className="user-management">
      <div className="user-management-header">
        <div>
          <h2>👥 User Management</h2>
          <p className="subtitle">Manage system users, roles, and permissions</p>
        </div>
        <button onClick={() => setShowCreateUser(!showCreateUser)} className="create-user-btn">
          + Create New User
        </button>
      </div>

      {message.text && (
        <div className={`message ${message.type}`}>
          {message.text}
        </div>
      )}

      {/* Statistics Cards */}
      <div className="user-stats">
        <div className="stat-card">
          <div className="stat-icon">👥</div>
          <div className="stat-info">
            <span className="stat-label">Total Users</span>
            <span className="stat-value">{users.length}</span>
          </div>
        </div>
        <div className="stat-card admin">
          <div className="stat-icon">👑</div>
          <div className="stat-info">
            <span className="stat-label">Admins</span>
            <span className="stat-value">{roleCounts.admin}</span>
          </div>
        </div>
        <div className="stat-card analyst">
          <div className="stat-icon">📊</div>
          <div className="stat-info">
            <span className="stat-label">Analysts</span>
            <span className="stat-value">{roleCounts.analyst}</span>
          </div>
        </div>
        <div className="stat-card viewer">
          <div className="stat-icon">👁️</div>
          <div className="stat-info">
            <span className="stat-label">Viewers</span>
            <span className="stat-value">{roleCounts.viewer}</span>
          </div>
        </div>
      </div>

      {/* Create User Form - Admin can create ANY role */}
      {showCreateUser && (
        <div className="create-user-form">
          <h3>Create New User</h3>
          <form onSubmit={handleCreateUser}>
            <div className="form-row">
              <input
                type="text"
                placeholder="Username"
                value={newUser.username}
                onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                required
              />
              <input
                type="email"
                placeholder="Email"
                value={newUser.email}
                onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                required
              />
              <input
                type="password"
                placeholder="Password"
                value={newUser.password}
                onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                required
              />
              <select
                value={newUser.roles[0]}
                onChange={(e) => setNewUser({ ...newUser, roles: [e.target.value] })}
              >
                <option value="admin">👑 Admin - Full access</option>
                <option value="analyst">📊 Analyst - View records and insights</option>
                <option value="viewer">👁️ Viewer - View only</option>
              </select>
              <button type="submit">Create User</button>
              <button type="button" onClick={() => setShowCreateUser(false)}>Cancel</button>
            </div>
          </form>
        </div>
      )}

      {/* Users Table */}
      <div className="users-table-container">
        <table className="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 ? (
              <tr>
                <td colSpan="6" className="no-data">No users found</td>
              </tr>
            ) : (
              users.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>
                    <div className="user-cell">
                      <span className="user-avatar" style={{ background: user.roles[0]?.name === 'ROLE_ADMIN' ? '#ffd700' : user.roles[0]?.name === 'ROLE_ANALYST' ? '#10b981' : '#667eea' }}>
                        {getRoleIcon(user.roles[0]?.name)}
                      </span>
                      <span className="username">{user.username}</span>
                    </div>
                  </td>
                  <td>{user.email}</td>
                  <td>
                    <select
                      value={user.roles[0]?.name || 'ROLE_VIEWER'}
                      onChange={(e) => handleRoleChange(user.id, e.target.value)}
                      className={`role-select ${getRoleBadgeClass(user.roles[0]?.name)}`}
                    >
                      <option value="ROLE_ADMIN">👑 Admin</option>
                      <option value="ROLE_ANALYST">📊 Analyst</option>
                      <option value="ROLE_VIEWER">👁️ Viewer</option>
                    </select>
                  </td>
                  <td>
                    <button
                      onClick={() => handleStatusToggle(user.id, user.active)}
                      className={`status-badge ${user.active ? 'active' : 'inactive'}`}
                    >
                      {user.active ? 'Active' : 'Inactive'}
                    </button>
                  </td>
                  <td className="actions-cell">
                    <button
                      onClick={() => handleStatusToggle(user.id, user.active)}
                      className={`action-btn ${user.active ? 'deactivate' : 'activate'}`}
                      title={user.active ? 'Deactivate User' : 'Activate User'}
                    >
                      {user.active ? '🔒' : '🔓'}
                    </button>
                    <button
                      onClick={() => handleDeleteUser(user.id)}
                      className="action-btn delete"
                      title="Delete User"
                    >
                      🗑️
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Role Information Section */}
      <div className="role-info-section">
        <h3>📋 Role Descriptions & Permissions</h3>
        <div className="role-cards">
          <div className="role-card admin">
            <div className="role-icon">👑</div>
            <div className="role-details">
              <h4>Administrator</h4>
              <p>Full system access. Can create, edit, delete records and manage all users.</p>
              <ul>
                <li>✅ View dashboard and records</li>
                <li>✅ Create, edit, delete records</li>
                <li>✅ Manage all users</li>
                <li>✅ Change user roles</li>
                <li>✅ Create admin users</li>
              </ul>
            </div>
          </div>
          <div className="role-card analyst">
            <div className="role-icon">📊</div>
            <div className="role-details">
              <h4>Analyst</h4>
              <p>Can view all data and access insights but cannot modify records.</p>
              <ul>
                <li>✅ View dashboard and records</li>
                <li>✅ Access financial insights</li>
                <li>❌ Cannot create/edit/delete records</li>
                <li>❌ Cannot manage users</li>
              </ul>
            </div>
          </div>
          <div className="role-card viewer">
            <div className="role-icon">👁️</div>
            <div className="role-details">
              <h4>Viewer</h4>
              <p>Read-only access to dashboard and records.</p>
              <ul>
                <li>✅ View dashboard</li>
                <li>✅ View records</li>
                <li>❌ Cannot modify records</li>
                <li>❌ Cannot access insights</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default UserManagement;