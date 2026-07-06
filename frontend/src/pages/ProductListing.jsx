import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../services/productService';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import { formatPrice } from '../utils/currency';
import { getProductImageUrl, handleProductImageError } from '../utils/productImage';

export default function ProductListing() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { addToCart } = useCart();
  const { isAuthenticated, isAdmin } = useAuth();
  const { showToast } = useToast();

  const loadCategories = async () => {
    try {
      const data = await productService.getCategories();
      setCategories(data || []);
    } catch {
      setCategories([]);
    }
  };

  const loadProducts = async () => {
    setLoading(true);
    setError('');
    try {
      let data;
      if (categoryId) {
        const list = await productService.getProductsByCategory(categoryId);
        const filtered = search
          ? list.filter((p) => p.name.toLowerCase().includes(search.toLowerCase()))
          : list;
        setProducts(filtered);
        setTotalPages(1);
      } else if (search.trim()) {
        data = await productService.searchProducts(search.trim(), page, 12);
        setProducts(data.content || []);
        setTotalPages(data.totalPages || 0);
      } else {
        data = await productService.getProducts(page, 12);
        setProducts(data.content || []);
        setTotalPages(data.totalPages || 0);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    loadProducts();
  }, [page, categoryId]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    loadProducts();
  };

  const handleAddToCart = (product) => {
    if (!isAuthenticated || isAdmin) {
      showToast('Please login as a customer to add items to cart', 'error');
      return;
    }
    addToCart(product, 1);
    showToast(`${product.name} added to cart`);
  };

  return (
    <div className="page container">
      <div className="page-header">
        <h1>Products</h1>
        <p>Browse our catalog and find what you need.</p>
      </div>

      <form className="filters-bar" onSubmit={handleSearch}>
        <input
          type="text"
          placeholder="Search products..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="input"
        />
        <select
          value={categoryId}
          onChange={(e) => {
            setCategoryId(e.target.value);
            setPage(0);
          }}
          className="input"
        >
          <option value="">All Categories</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </select>
        <button type="submit" className="btn btn-primary">
          Search
        </button>
      </form>

      {loading && <LoadingSpinner />}
      <ErrorMessage message={error} onRetry={loadProducts} />

      {!loading && !error && (
        <>
          <div className="product-grid">
            {products.length === 0 ? (
              <p className="empty-state">No products found.</p>
            ) : (
              products.map((product) => (
                <div key={product.id} className="product-card">
                  <Link to={`/products/${product.id}`} className="product-image">
                    <img
                      src={getProductImageUrl(product)}
                      alt={product.name}
                      onError={(e) => handleProductImageError(e, product)}
                    />
                  </Link>
                  <div className="product-info">
                    <Link to={`/products/${product.id}`}>
                      <h3>{product.name}</h3>
                    </Link>
                    <p className="product-desc">{product.description?.slice(0, 80)}</p>
                    <div className="product-actions">
                      <span className="product-price">{formatPrice(product.price)}</span>
                      <button
                        type="button"
                        className="btn btn-primary btn-sm"
                        onClick={() => handleAddToCart(product)}
                      >
                        Add to Cart
                      </button>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>

          {totalPages > 1 && !categoryId && (
            <div className="pagination">
              <button
                type="button"
                className="btn btn-outline btn-sm"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                Previous
              </button>
              <span>
                Page {page + 1} of {totalPages}
              </span>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
