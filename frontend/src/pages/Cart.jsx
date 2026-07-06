import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useToast } from '../context/ToastContext';
import { formatPrice } from '../utils/currency';
import { getProductImageUrl, handleProductImageError } from '../utils/productImage';

export default function Cart() {
  const { items, subtotal, updateQuantity, removeFromCart } = useCart();
  const { showToast } = useToast();

  const handleRemove = (productId, name) => {
    removeFromCart(productId);
    showToast(`${name} removed from cart`);
  };

  if (items.length === 0) {
    return (
      <div className="page">
        <div className="page-header">
          <h1>Shopping Cart</h1>
        </div>
        <div className="empty-cart">
          <p>Your cart is empty.</p>
          <Link to="/products" className="btn btn-primary">Continue Shopping</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Shopping Cart</h1>
        <p>{items.length} item(s) in your cart</p>
      </div>

      <div className="cart-layout">
        <div className="cart-items">
          {items.map((item) => (
            <div key={item.productId} className="cart-item">
              <div className="cart-item-image">
                <img
                  src={getProductImageUrl(item)}
                  alt={item.name}
                  onError={(e) => handleProductImageError(e, item)}
                />
              </div>
              <div className="cart-item-info">
                <h3>{item.name}</h3>
                <p className="product-price">{formatPrice(item.price)}</p>
              </div>
              <div className="cart-item-qty">
                <button type="button" className="qty-btn" onClick={() => updateQuantity(item.productId, item.quantity - 1)}>−</button>
                <span>{item.quantity}</span>
                <button type="button" className="qty-btn" onClick={() => updateQuantity(item.productId, item.quantity + 1)}>+</button>
              </div>
              <div className="cart-item-total">
                {formatPrice(item.price * item.quantity)}
              </div>
              <button type="button" className="btn-icon" onClick={() => handleRemove(item.productId, item.name)} aria-label="Remove">
                &times;
              </button>
            </div>
          ))}
        </div>

        <div className="cart-summary">
          <h3>Order Summary</h3>
          <div className="summary-row">
            <span>Subtotal</span>
            <span>{formatPrice(subtotal)}</span>
          </div>
          <div className="summary-row total">
            <span>Total</span>
            <span>{formatPrice(subtotal)}</span>
          </div>
          <Link to="/checkout" className="btn btn-primary btn-block">
            Proceed to Checkout
          </Link>
          <Link to="/products" className="btn btn-outline btn-block">
            Continue Shopping
          </Link>
        </div>
      </div>
    </div>
  );
}
