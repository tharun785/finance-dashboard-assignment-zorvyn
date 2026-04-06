import React, { useState, useEffect, useCallback } from 'react';
import { getDashboardSummary, getRecentActivity } from '../services/api';
import './Dashboard.css';

function Dashboard() {
  const [summary, setSummary] = useState({
    totalIncome: 0,
    totalExpense: 0,
    netBalance: 0,
    categoryWise: {}
  });
  const [recentActivity, setRecentActivity] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 18) return 'Good Afternoon';
    return 'Good Evening';
  };

  const getRoleBadge = () => {
    const role = user.roles?.[0]?.replace('ROLE_', '');
    if (role === 'ADMIN') return { name: 'Administrator', icon: '👑', color: '#ffd700' };
    if (role === 'ANALYST') return { name: 'Financial Analyst', icon: '📊', color: '#10b981' };
    return { name: 'Viewer', icon: '👁️', color: '#667eea' };
  };

  const roleBadge = getRoleBadge();

  const fetchDashboardData = useCallback(async () => {
    try {
      setLoading(true);
      console.log('Fetching dashboard data...');

      const [summaryRes, activityRes] = await Promise.all([
        getDashboardSummary(),
        getRecentActivity()
      ]);

      console.log('Dashboard Summary:', summaryRes.data);
      console.log('Recent Activity:', activityRes.data);

      setSummary(summaryRes.data);
      setRecentActivity(activityRes.data || []);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData, refreshKey]);

  // Function to refresh dashboard data (call this after creating/updating/deleting records)
  const refreshDashboard = () => {
    console.log('Refreshing dashboard data...');
    setRefreshKey(prev => prev + 1);
  };

  // Expose refresh function to window so Records component can call it
  useEffect(() => {
    window.refreshDashboard = refreshDashboard;
    return () => {
      delete window.refreshDashboard;
    };
  }, []);

  const getCategoryData = () => {
    const categoryWise = summary.categoryWise || {};
    const totalExpense = summary.totalExpense || 0;

    return Object.entries(categoryWise).map(([name, value]) => ({
      name,
      value,
      percentage: totalExpense > 0 ? (value / totalExpense) * 100 : 0
    }));
  };

  const getMaxValue = () => {
    const values = Object.values(summary.categoryWise || {});
    return Math.max(...values, 0);
  };

  if (loading) {
    return <div className="loading">Loading dashboard...</div>;
  }

  return (
    <div className="dashboard">
      <div className="welcome-section">
        <div className="welcome-content">
          <div className="welcome-text">
            <h1>
              {getGreeting()}, {user.username}!
              <span className="welcome-wave"> 👋</span>
            </h1>
            <p>Welcome to your Finance Dashboard. Here's your financial overview.</p>
          </div>
          <div className="role-badge" style={{ backgroundColor: roleBadge.color + '20', borderColor: roleBadge.color }}>
            <span className="role-icon">{roleBadge.icon}</span>
            <div>
              <div className="role-label">Current Role</div>
              <div className="role-name" style={{ color: roleBadge.color }}>{roleBadge.name}</div>
            </div>
          </div>
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card income">
          <div className="stat-icon">💰</div>
          <div className="stat-info">
            <h3>Total Income</h3>
            <p className="amount">${(summary.totalIncome || 0).toFixed(2)}</p>
          </div>
        </div>
        <div className="stat-card expense">
          <div className="stat-icon">💸</div>
          <div className="stat-info">
            <h3>Total Expense</h3>
            <p className="amount">${(summary.totalExpense || 0).toFixed(2)}</p>
          </div>
        </div>
        <div className="stat-card balance">
          <div className="stat-icon">⚖️</div>
          <div className="stat-info">
            <h3>Net Balance</h3>
            <p className={`amount ${(summary.netBalance || 0) >= 0 ? 'positive' : 'negative'}`}>
              ${(summary.netBalance || 0).toFixed(2)}
            </p>
          </div>
        </div>
      </div>

      <div className="charts-container">
        <div className="chart-card">
          <h3>📊 Category-wise Expenses</h3>
          <div className="bar-chart">
            {getCategoryData().length === 0 ? (
              <p className="no-data">No expense data available. Add some records!</p>
            ) : (
              getCategoryData().map((category, index) => (
                <div key={index} className="bar-item">
                  <div className="bar-label">
                    <span className="category-name">{category.name}</span>
                    <span className="category-amount">${category.value.toFixed(2)}</span>
                  </div>
                  <div className="bar-background">
                    <div
                      className="bar-fill"
                      style={{
                        width: `${(category.value / getMaxValue()) * 100}%`,
                        backgroundColor: `hsl(${index * 45}, 70%, 50%)`
                      }}
                    >
                      <span className="bar-percentage">{category.percentage.toFixed(1)}%</span>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="chart-card">
          <h3>🕒 Recent Transactions</h3>
          <div className="activity-list">
            {recentActivity.length === 0 ? (
              <p className="no-data">No recent transactions. Add some records!</p>
            ) : (
              recentActivity.map((activity) => (
                <div key={activity.id} className="activity-item">
                  <div className="activity-info">
                    <span className="activity-category">{activity.category}</span>
                    <span className="activity-description">{activity.description || 'No description'}</span>
                    <span className="activity-date">
                      {new Date(activity.date).toLocaleDateString()}
                    </span>
                  </div>
                  <div className={`activity-amount ${activity.type === 'INCOME' ? 'income' : 'expense'}`}>
                    {activity.type === 'INCOME' ? '+' : '-'}${activity.amount.toFixed(2)}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      <div className="quick-stats">
        <h3>💡 Quick Insights</h3>
        <div className="insights-grid">
          <div className="insight-card">
            <span className="insight-label">Savings Rate</span>
            <span className="insight-value">
              {(summary.totalIncome || 0) > 0
                ? (((summary.netBalance || 0) / (summary.totalIncome || 1)) * 100).toFixed(1)
                : 0}%
            </span>
          </div>
          <div className="insight-card">
            <span className="insight-label">Expense to Income Ratio</span>
            <span className="insight-value">
              {(summary.totalIncome || 0) > 0
                ? (((summary.totalExpense || 0) / (summary.totalIncome || 1)) * 100).toFixed(1)
                : 0}%
            </span>
          </div>
          <div className="insight-card">
            <span className="insight-label">Total Categories</span>
            <span className="insight-value">{Object.keys(summary.categoryWise || {}).length}</span>
          </div>
          <div className="insight-card">
            <span className="insight-label">Total Transactions</span>
            <span className="insight-value">{recentActivity.length}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;