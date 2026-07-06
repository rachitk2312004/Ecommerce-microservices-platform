import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { productService } from '../../services/productService';
import { useToast } from '../../context/ToastContext';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';

export default function EditProduct() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({
    name: '',
    description: '',
    price: '',
    categoryId: '',
    imageUrl: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const [product, cats] = await Promise.all([
          productService.getProductById(id),
          productService.getCategories(),
        ]);
        setCategories(cats || []);
        setForm({
          name: product.name || '',
          description: product.description || '',
          price: String(product.price),
          categoryId: String(product.categoryId),
          imageUrl: product.imageUrl || '',
        });
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await productService.updateProduct(id, {
        ...form,
        price: Number(form.price),
        categoryId: Number(form.categoryId),
      });
      showToast('Product updated');
      navigate('/admin/products');
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingSpinner fullPage />;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Edit Product</h1>
      </div>

      <ErrorMessage message={error} />

      <form onSubmit={handleSubmit} className="form-card">
        <div className="form-group">
          <label htmlFor="name">Name</label>
          <input id="name" name="name" required value={form.name} onChange={handleChange} className="input" />
        </div>
        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea id="description" name="description" rows={4} value={form.description} onChange={handleChange} className="input" />
        </div>
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="price">Price (₹)</label>
            <input id="price" name="price" type="number" step="0.01" min="0.01" required value={form.price} onChange={handleChange} className="input" />
          </div>
          <div className="form-group">
            <label htmlFor="categoryId">Category</label>
            <select id="categoryId" name="categoryId" required value={form.categoryId} onChange={handleChange} className="input">
              <option value="">Select category</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
          </div>
        </div>
        <div className="form-group">
          <label htmlFor="imageUrl">Image URL</label>
          <input id="imageUrl" name="imageUrl" value={form.imageUrl} onChange={handleChange} className="input" placeholder="https://images.unsplash.com/photo-..." />
          <p className="form-hint">Paste a direct product image URL (Unsplash or similar).</p>
        </div>
        <div className="form-actions">
          <button type="button" className="btn btn-outline" onClick={() => navigate('/admin/products')}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
        </div>
      </form>
    </div>
  );
}
