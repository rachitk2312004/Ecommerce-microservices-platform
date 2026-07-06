import { useEffect, useState } from 'react';
import { productService } from '../../services/productService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { useToast } from '../../context/ToastContext';

export default function CategoryManagement() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ name: '', description: '' });
  const [editId, setEditId] = useState(null);
  const [deleteId, setDeleteId] = useState(null);
  const [saving, setSaving] = useState(false);
  const { showToast } = useToast();

  const loadCategories = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await productService.getCategories();
      setCategories(data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  const resetForm = () => {
    setForm({ name: '', description: '' });
    setEditId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (editId) {
        await productService.updateCategory(editId, form);
        showToast('Category updated');
      } else {
        await productService.createCategory(form);
        showToast('Category created');
      }
      resetForm();
      loadCategories();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setSaving(false);
    }
  };

  const startEdit = (cat) => {
    setEditId(cat.id);
    setForm({ name: cat.name, description: cat.description || '' });
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await productService.deleteCategory(deleteId);
      showToast('Category deactivated');
      setDeleteId(null);
      loadCategories();
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Category Management</h1>
      </div>

      <form onSubmit={handleSubmit} className="form-card inline-form">
        <h3>{editId ? 'Edit Category' : 'Add Category'}</h3>
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="name">Name</label>
            <input id="name" name="name" required value={form.name} onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))} className="input" />
          </div>
          <div className="form-group">
            <label htmlFor="description">Description</label>
            <input id="description" name="description" value={form.description} onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))} className="input" />
          </div>
        </div>
        <div className="form-actions">
          {editId && (
            <button type="button" className="btn btn-outline" onClick={resetForm}>Cancel Edit</button>
          )}
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving...' : editId ? 'Update' : 'Add Category'}
          </button>
        </div>
      </form>

      {loading && <LoadingSpinner />}
      <ErrorMessage message={error} onRetry={loadCategories} />

      {!loading && !error && (
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Description</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((cat) => (
                <tr key={cat.id}>
                  <td>{cat.name}</td>
                  <td>{cat.description || '—'}</td>
                  <td>
                    <span className={`badge ${cat.active ? 'badge-confirmed' : 'badge-cancelled'}`}>
                      {cat.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="actions-cell">
                    <button type="button" className="btn btn-outline btn-sm" onClick={() => startEdit(cat)}>Edit</button>
                    <button type="button" className="btn btn-danger btn-sm" onClick={() => setDeleteId(cat.id)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Deactivate Category"
        message="Are you sure you want to deactivate this category?"
        confirmLabel="Deactivate"
        onConfirm={handleDelete}
        onCancel={() => setDeleteId(null)}
        danger
      />
    </div>
  );
}
