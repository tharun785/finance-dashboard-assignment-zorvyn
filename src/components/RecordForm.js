import React, { useState } from 'react';
import { createRecord } from '../services/api';
import './RecordForm.css';

function RecordForm({ onRecordAdded }) {
  const [formData, setFormData] = useState({
    amount: '',
    type: 'EXPENSE',
    category: '',
    date: new Date().toISOString().split('T')[0],
    description: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await createRecord({
        ...formData,
        amount: parseFloat(formData.amount)
      });
      setFormData({
        amount: '',
        type: 'EXPENSE',
        category: '',
        date: new Date().toISOString().split('T')[0],
        description: ''
      });
      onRecordAdded();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create record');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="record-form">
      <h3>Add New Record</h3>
      {error && <div className="error-message">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="form-group">
            <input
              type="number"
              name="amount"
              placeholder="Amount"
              value={formData.amount}
              onChange={handleChange}
              required
              step="0.01"
            />
          </div>
          <div className="form-group">
            <select name="type" value={formData.type} onChange={handleChange} required>
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
          </div>
          <div className="form-group">
            <input
              type="text"
              name="category"
              placeholder="Category"
              value={formData.category}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <input
              type="date"
              name="date"
              value={formData.date}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <input
              type="text"
              name="description"
              placeholder="Description (optional)"
              value={formData.description}
              onChange={handleChange}
            />
          </div>
          <button type="submit" disabled={loading}>
            {loading ? 'Adding...' : 'Add Record'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default RecordForm;