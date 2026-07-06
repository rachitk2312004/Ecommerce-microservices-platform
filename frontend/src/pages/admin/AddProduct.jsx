import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../../services/productService';
import { useToast } from '../../context/ToastContext';
import ErrorMessage from '../../components/common/ErrorMessage';

export default function AddProduct() {
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
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    productService.getCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await productService.createProduct({
        ...form,
        price: Number(form.price),
        categoryId: Number(form.categoryId),
      });
      showToast('Product created');
      navigate('/admin/products');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Add Product</h1>
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
            <input id="price" name="price" type="number" step="0.01" min="0.01" required value={form.price} onChange={handleChange} className="input" placeholder="14999" />
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
          <input
            id="imageUrl"
            name="imageUrl"
            value={form.imageUrl}
            onChange={handleChange}
            className="input"
            placeholder="https://images.unsplash.com/photo-... (product-related image)"
          />
          <p className="form-hint">Use a direct image link related to the product (e.g. Unsplash). Example for headphones: https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=300&fit=crop</p>
        </div>
        <div className="form-actions">
          <button type="button" className="btn btn-outline" onClick={() => navigate('/admin/products')}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating...' : 'Create Product'}
          </button>
        </div>
      </form>
    </div>
  );
}
