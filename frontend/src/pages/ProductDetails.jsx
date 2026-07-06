import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productService } from '../services/productService';
import { inventoryService } from '../services/inventoryService';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import { formatPrice } from '../utils/currency';
import { getProductImageUrl, handleProductImageError } from '../utils/productImage';

export default function ProductDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [stock, setStock] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { addToCart } = useCart();
  const { isAuthenticated, isAdmin } = useAuth();
  const { showToast } = useToast();

  const loadProduct = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await productService.getProductById(id);
      setProduct(data);
      try {
        const inv = await inventoryService.getByProductId(id);
        setStock(inv?.availableQuantity ?? null);
      } catch {
        setStock(null);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProduct();
  }, [id]);

  const handleAddToCart = () => {
    if (!isAuthenticated || isAdmin) {
      showToast('Please login as a customer to add items to cart', 'error');
      navigate('/login');
      return;
    }
    addToCart(product, quantity);
    showToast(`${product.name} added to cart`);
  };

  if (loading) return <LoadingSpinner fullPage />;
  if (error) return <div className="container page"><ErrorMessage message={error} onRetry={loadProduct} /></div>;
  if (!product) return null;

  return (
    <div className="page container">
      <div className="product-detail">
        <div className="product-detail-image">
          <img
            src={getProductImageUrl(product)}
            alt={product.name}
            onError={(e) => handleProductImageError(e, product)}
          />
        </div>
        <div className="product-detail-info">
          <h1>{product.name}</h1>
          <p className="product-price large">{formatPrice(product.price)}</p>
          {stock !== null && (
            <p className={`stock-badge${stock > 0 ? '' : ' out'}`}>
              {stock > 0 ? `${stock} in stock` : 'Out of stock'}
            </p>
          )}
          <p className="product-description">{product.description || 'No description available.'}</p>

          {!isAdmin && (
            <div className="quantity-row">
              <label htmlFor="qty">Quantity</label>
              <input
                id="qty"
                type="number"
                min="1"
                max={stock || 99}
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
                className="input input-sm"
              />
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleAddToCart}
                disabled={stock === 0}
              >
                Add to Cart
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
