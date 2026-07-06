import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../../services/productService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { useToast } from '../../context/ToastContext';
import { formatPrice } from '../../utils/currency';

export default function ProductManagement() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [deleteId, setDeleteId] = useState(null);
  const { showToast } = useToast();

  const loadProducts = async () => {
    setLoading(true);
    setError('');
    try {
      const page = await productService.getProducts(0, 50);
      setProducts(page.content || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await productService.deleteProduct(deleteId);
      showToast('Product deactivated');
      setDeleteId(null);
      loadProducts();
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Product Management</h1>
        <Link to="/admin/products/new" className="btn btn-primary btn-sm">Add Product</Link>
      </div>

      {loading && <LoadingSpinner />}
      <ErrorMessage message={error} onRetry={loadProducts} />

      {!loading && !error && (
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Price</th>
                <th>Category</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td>{product.name}</td>
                  <td>{formatPrice(product.price)}</td>
                  <td>{product.categoryId}</td>
                  <td>
                    <span className={`badge ${product.active ? 'badge-confirmed' : 'badge-cancelled'}`}>
                      {product.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="actions-cell">
                    <Link to={`/admin/products/${product.id}/edit`} className="btn btn-outline btn-sm">Edit</Link>
                    <button type="button" className="btn btn-danger btn-sm" onClick={() => setDeleteId(product.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Deactivate Product"
        message="Are you sure you want to deactivate this product?"
        confirmLabel="Deactivate"
        onConfirm={handleDelete}
        onCancel={() => setDeleteId(null)}
        danger
      />
    </div>
  );
}
