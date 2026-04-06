import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../services/api';
import './Login.css';

function Login({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      console.log('Attempting login with:', username);
      const response = await login(username, password);
      console.log('Login response:', response.data);

      const { token, id, username: userName, email, roles } = response.data;

      const userData = { id, username: userName, email, roles };

      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userData));

      // Notify App component about login
      if (onLogin) {
        onLogin(userData);
      }

      console.log('Login successful, redirecting to dashboard');
      navigate('/dashboard');
    } catch (err) {
      console.error('Login error:', err);

      if (err.response) {
        setError(err.response.data?.message || 'Login failed. Please check your credentials.');
      } else if (err.request) {
        setError('Cannot connect to server. Make sure backend is running on port 8080');
      } else {
        setError('An error occurred. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2>Finance Dashboard</h2>
        <h3>Login</h3>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div className="signup-link">
          Don't have an account? <Link to="/signup">Sign up here</Link>
        </div>

        <div className="demo-credentials">
          <p><strong>Demo Credentials:</strong></p>
          <p>Admin: admin / admin123</p>
          <p>Analyst: analyst / analyst123</p>
          <p>Viewer: viewer / viewer123</p>
        </div>
      </div>
    </div>
  );
}

export default Login;