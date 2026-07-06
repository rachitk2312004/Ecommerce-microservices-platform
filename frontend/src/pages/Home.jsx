import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../services/productService';
import { formatPrice } from '../utils/currency';
import { getProductImageUrl, handleProductImageError } from '../utils/productImage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';

export default function Home() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadFeatured = async () => {
    setLoading(true);
    setError('');
    try {
      const page = await productService.getProducts(0, 6);
      setProducts(page.content || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFeatured();
  }, []);

  return (
    <div className="page home-page">
      <section className="hero">
        <div className="container hero-content">
          <h1>Discover Quality Products</h1>
          <p>Shop the latest arrivals with fast checkout and secure payments.</p>
          <Link to="/products" className="btn btn-primary btn-lg">
            Browse Products
          </Link>
        </div>
      </section>

      <section className="container section">
        <div className="section-header">
          <h2>Featured Products</h2>
          <Link to="/products" className="link-arrow">
            View all
          </Link>
        </div>

        {loading && <LoadingSpinner />}
        <ErrorMessage message={error} onRetry={loadFeatured} />

        {!loading && !error && (
          <div className="product-grid">
            {products.map((product) => (
              <Link key={product.id} to={`/products/${product.id}`} className="product-card">
                <div className="product-image">
                  <img
                    src={getProductImageUrl(product)}
                    alt={product.name}
                    onError={(e) => handleProductImageError(e, product)}
                  />
                </div>
                <div className="product-info">
                  <h3>{product.name}</h3>
                  <p className="product-price">{formatPrice(product.price)}</p>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
