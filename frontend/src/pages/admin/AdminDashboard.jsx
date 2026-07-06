import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../../services/productService';
import { orderService } from '../../services/orderService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';

export default function AdminDashboard() {
  const [stats, setStats] = useState({ products: 0, categories: 0, orders: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadStats = async () => {
    setLoading(true);
    setError('');
    try {
      const [productsPage, categories, orders] = await Promise.all([
        productService.getProducts(0, 1),
        productService.getCategories(),
        orderService.getMyOrders().catch(() => []),
      ]);
      setStats({
        products: productsPage.totalElements || 0,
        categories: (categories || []).length,
        orders: (orders || []).length,
      });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStats();
  }, []);

  return (
    <div className="page">
      <div className="page-header">
        <h1>Admin Dashboard</h1>
        <p>Overview of your store management.</p>
      </div>

      {loading && <LoadingSpinner />}
      <ErrorMessage message={error} onRetry={loadStats} />

      {!loading && !error && (
        <>
          <div className="dashboard-cards">
            <div className="stat-card">
              <span className="stat-label">Products</span>
              <span className="stat-value">{stats.products}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Categories</span>
              <span className="stat-value">{stats.categories}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Your Orders</span>
              <span className="stat-value">{stats.orders}</span>
            </div>
          </div>

          <div className="admin-quick-links">
            <Link to="/admin/products" className="quick-link-card">
              <h3>Manage Products</h3>
              <p>Add, edit, or deactivate products</p>
            </Link>
            <Link to="/admin/categories" className="quick-link-card">
              <h3>Manage Categories</h3>
              <p>Organize your product catalog</p>
            </Link>
            <Link to="/admin/inventory" className="quick-link-card">
              <h3>Manage Inventory</h3>
              <p>Update stock levels</p>
            </Link>
          </div>
        </>
      )}
    </div>
  );
}
