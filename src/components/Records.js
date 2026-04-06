import React, { useState, useEffect } from 'react';
import { getRecords, createRecord, updateRecord, deleteRecord } from '../services/api';
import './Records.css';

function Records() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [filters, setFilters] = useState({ type: '', category: '', startDate: '', endDate: '' });
  const [formData, setFormData] = useState({
    amount: '',
    type: 'EXPENSE',
    category: '',
    date: new Date().toISOString().split('T')[0],
    description: ''
  });
  const [user, setUser] = useState(null);
  const [summary, setSummary] = useState({ totalIncome: 0, totalExpense: 0 });

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      const parsedUser = JSON.parse(userData);
      setUser(parsedUser);
      console.log('Records - User loaded:', parsedUser);
    }
  }, []);

  useEffect(() => {
    if (user) {
      fetchRecords();
    }
  }, [filters, user]);

  const userRoles = user?.roles || [];
  const isAdmin = userRoles.includes('ROLE_ADMIN');
  const isAnalyst = userRoles.includes('ROLE_ANALYST');
  const isViewer = userRoles.includes('ROLE_VIEWER') || (!isAdmin && !isAnalyst);

  const canModify = isAdmin;

  const getRoleMessage = () => {
    if (isAdmin) return '✅ Full Access: You can create, edit, and delete records';
    if (isAnalyst) return '📊 View Only: You can view records and access insights';
    return '👁️ View Only: You can view dashboard and records';
  };

  const fetchRecords = async () => {
    try {
      setLoading(true);
      console.log('Fetching records with filters:', filters);

      const response = await getRecords(filters);
      console.log('Full API Response:', response.data);

      let recordsData = [];
      if (response.data && response.data.records) {
        recordsData = response.data.records;
        setSummary({
          totalIncome: response.data.totalIncome || 0,
          totalExpense: response.data.totalExpense || 0
        });
      } else if (response.data && Array.isArray(response.data)) {
        recordsData = response.data;
      } else {
        recordsData = [];
      }

      console.log('Processed records:', recordsData);
      setRecords(recordsData);
    } catch (error) {
      console.error('Error fetching records:', error);
      setRecords([]);
    } finally {
      setLoading(false);
    }
  };

  // Function to refresh dashboard after changes
  const refreshDashboard = () => {
    console.log('Refreshing dashboard...');
    if (window.refreshDashboard) {
      window.refreshDashboard();
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canModify) {
      alert('Only Admin can create/edit records');
      return;
    }

    console.log('Submitting record:', formData);

    try {
      if (editingRecord) {
        await updateRecord(editingRecord.id, formData);
        alert('Record updated successfully!');
      } else {
        await createRecord(formData);
        alert('Record created successfully!');
      }

      resetForm();
      await fetchRecords();
      refreshDashboard(); // Refresh dashboard after creating/updating
    } catch (error) {
      console.error('Error saving record:', error);
      alert('Failed to save record: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleDelete = async (id) => {
    if (!canModify) {
      alert('Only Admin can delete records');
      return;
    }
    if (window.confirm('Are you sure you want to delete this record?')) {
      try {
        await deleteRecord(id);
        alert('Record deleted successfully!');
        await fetchRecords();
        refreshDashboard(); // Refresh dashboard after deleting
      } catch (error) {
        console.error('Error deleting record:', error);
        alert('Failed to delete record');
      }
    }
  };

  const handleEdit = (record) => {
    if (!canModify) {
      alert('Only Admin can edit records');
      return;
    }
    setEditingRecord(record);
    setFormData({
      amount: record.amount,
      type: record.type,
      category: record.category,
      date: record.date,
      description: record.description || ''
    });
    setShowForm(true);
  };

  const resetForm = () => {
    setEditingRecord(null);
    setFormData({
      amount: '',
      type: 'EXPENSE',
      category: '',
      date: new Date().toISOString().split('T')[0],
      description: ''
    });
    setShowForm(false);
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const clearFilters = () => {
    setFilters({ type: '', category: '', startDate: '', endDate: '' });
  };

  const getTotalByType = (type) => {
    return records
      .filter(r => r.type === type)
      .reduce((sum, r) => sum + r.amount, 0);
  };

  if (loading) return <div className="loading">Loading records...</div>;
  if (!user) return <div className="loading">Loading user data...</div>;

  return (
    <div className="records-page">
      <div className="records-header">
        <h1>📝 Financial Records</h1>
        {canModify && (
          <button onClick={() => setShowForm(true)} className="add-record-btn">
            + Add New Record
          </button>
        )}
      </div>

      <div className="role-info-banner">
        <div className="role-badge-info">
          <span className="role-icon">
            {isAdmin && '👑'}
            {isAnalyst && '📊'}
            {isViewer && '👁️'}
          </span>
          <div>
            <div className="role-title">
              Logged in as: <strong>{isAdmin ? 'ADMIN' : isAnalyst ? 'ANALYST' : 'VIEWER'}</strong>
            </div>
            <div className="role-permissions">
              {getRoleMessage()}
            </div>
          </div>
        </div>
      </div>

      <div className="records-summary">
        <div className="summary-card income">
          <span>Total Income</span>
          <strong>${(summary.totalIncome || getTotalByType('INCOME')).toFixed(2)}</strong>
        </div>
        <div className="summary-card expense">
          <span>Total Expense</span>
          <strong>${(summary.totalExpense || getTotalByType('EXPENSE')).toFixed(2)}</strong>
        </div>
        <div className="summary-card balance">
          <span>Balance</span>
          <strong>${((summary.totalIncome || getTotalByType('INCOME')) - (summary.totalExpense || getTotalByType('EXPENSE'))).toFixed(2)}</strong>
        </div>
        <div className="summary-card count">
          <span>Total Records</span>
          <strong>{records.length}</strong>
        </div>
      </div>

      <div className="filters-section">
        <h3>🔍 Filters</h3>
        <div className="filters-grid">
          <select name="type" value={filters.type} onChange={handleFilterChange}>
            <option value="">All Types</option>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </select>
          <input
            type="text"
            name="category"
            placeholder="Category"
            value={filters.category}
            onChange={handleFilterChange}
          />
          <input
            type="date"
            name="startDate"
            value={filters.startDate}
            onChange={handleFilterChange}
          />
          <input
            type="date"
            name="endDate"
            value={filters.endDate}
            onChange={handleFilterChange}
          />
          <button onClick={clearFilters} className="clear-filters-btn">
            Clear Filters
          </button>
        </div>
      </div>

      {showForm && canModify && (
        <div className="modal-overlay" onClick={() => resetForm()}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingRecord ? 'Edit Record' : 'Add New Record'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Amount ($)</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.amount}
                  onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Type</label>
                <select
                  value={formData.type}
                  onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                  required
                >
                  <option value="INCOME">Income</option>
                  <option value="EXPENSE">Expense</option>
                </select>
              </div>
              <div className="form-group">
                <label>Category</label>
                <input
                  type="text"
                  value={formData.category}
                  onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                  required
                  placeholder="e.g., Food, Rent, Salary"
                />
              </div>
              <div className="form-group">
                <label>Date</label>
                <input
                  type="date"
                  value={formData.date}
                  onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Description (Optional)</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows="3"
                  placeholder="Enter description..."
                />
              </div>
              <div className="form-actions">
                <button type="submit" className="save-btn">
                  {editingRecord ? 'Update' : 'Create'}
                </button>
                <button type="button" onClick={resetForm} className="cancel-btn">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="records-table-container">
        <table className="records-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Type</th>
              <th>Category</th>
              <th>Amount</th>
              <th>Description</th>
              {canModify && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {records.length === 0 ? (
              <tr>
                <td colSpan={canModify ? 6 : 5} className="no-data">
                  No records found. {canModify && 'Click "Add New Record" to create one.'}
                </td>
              </tr>
            ) : (
              records.map((record) => (
                <tr key={record.id}>
                  <td>{new Date(record.date).toLocaleDateString()}</td>
                  <td className={`type-badge ${record.type.toLowerCase()}`}>
                    {record.type}
                  </td>
                  <td>{record.category}</td>
                  <td className={`amount ${record.type.toLowerCase()}`}>
                    ${record.amount.toFixed(2)}
                  </td>
                  <td>{record.description || '-'}</td>
                  {canModify && (
                    <td className="actions">
                      <button onClick={() => handleEdit(record)} className="edit-btn" title="Edit">
                        ✏️
                      </button>
                      <button onClick={() => handleDelete(record.id)} className="delete-btn" title="Delete">
                        🗑️
                      </button>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Records;