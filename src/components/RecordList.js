import React, { useState, useEffect } from 'react';
import { getRecords, deleteRecord } from '../services/api';
import './RecordList.css';

function RecordList({ refresh }) {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ type: '', category: '' });
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.roles?.includes('ROLE_ADMIN');

  useEffect(() => {
    fetchRecords();
  }, [filters, refresh]);

  const fetchRecords = async () => {
    try {
      const response = await getRecords(filters);
      setRecords(response.data.records);
    } catch (error) {
      console.error('Error fetching records:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this record?')) {
      try {
        await deleteRecord(id);
        fetchRecords();
      } catch (error) {
        console.error('Error deleting record:', error);
      }
    }
  };

  const handleFilterChange = (e) => {
    setFilters({ ...filters, [e.target.name]: e.target.value });
  };

  if (loading) return <div className="loading">Loading records...</div>;

  return (
    <div className="record-list">
      <div className="filters">
        <select name="type" onChange={handleFilterChange} value={filters.type}>
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
      </div>

      <div className="records-table">
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Type</th>
              <th>Category</th>
              <th>Amount</th>
              <th>Description</th>
              {isAdmin && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {records.length === 0 ? (
              <tr>
                <td colSpan={isAdmin ? 6 : 5} className="no-data">
                  No records found
                </td>
              </tr>
            ) : (
              records.map((record) => (
                <tr key={record.id}>
                  <td>{new Date(record.date).toLocaleDateString()}</td>
                  <td className={record.type.toLowerCase()}>{record.type}</td>
                  <td>{record.category}</td>
                  <td>${record.amount.toFixed(2)}</td>
                  <td>{record.description}</td>
                  {isAdmin && (
                    <td>
                      <button onClick={() => handleDelete(record.id)} className="delete-btn">
                        Delete
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

export default RecordList;