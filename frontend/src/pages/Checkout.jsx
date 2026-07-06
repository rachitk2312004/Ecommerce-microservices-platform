import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { orderService } from '../services/orderService';
import { useToast } from '../context/ToastContext';
import ErrorMessage from '../components/common/ErrorMessage';
import { formatPrice } from '../utils/currency';

const PAYMENT_METHODS = [
  { value: 'CARD', label: 'Credit / Debit Card' },
  { value: 'UPI', label: 'UPI' },
  { value: 'NET_BANKING', label: 'Net Banking' },
  { value: 'CASH_ON_DELIVERY', label: 'Cash on Delivery' },
];

export default function Checkout() {
  const { items, subtotal, checkoutItems, clearCart } = useCart();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (items.length === 0) {
    return (
      <div className="page">
        <div className="empty-cart">
          <p>Your cart is empty.</p>
          <Link to="/products" className="btn btn-primary">Shop Now</Link>
        </div>
      </div>
    );
  }

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const order = await orderService.createOrder({
        items: checkoutItems,
        paymentMethod,
      });
      clearCart();
      showToast('Order placed successfully!');
      navigate(`/order/success/${order.id}`, { state: { order } });
    } catch (err) {
      showToast(err.message || 'Order failed', 'error');
      navigate('/order/failure', { state: { message: err.message } });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Checkout</h1>
      </div>

      <ErrorMessage message={error} />

      <form onSubmit={handlePlaceOrder} className="checkout-layout">
        <div className="checkout-main">
          <section className="checkout-section">
            <h2>Order Items</h2>
            <ul className="checkout-items">
              {items.map((item) => (
                <li key={item.productId}>
                  <span>{item.name} × {item.quantity}</span>
                  <span>{formatPrice(item.price * item.quantity)}</span>
                </li>
              ))}
            </ul>
          </section>

          <section className="checkout-section">
            <h2>Payment Method</h2>
            <div className="payment-options">
              {PAYMENT_METHODS.map((method) => (
                <label key={method.value} className="payment-option">
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={method.value}
                    checked={paymentMethod === method.value}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                  />
                  {method.label}
                </label>
              ))}
            </div>
          </section>
        </div>

        <div className="cart-summary">
          <h3>Order Total</h3>
          <div className="summary-row">
            <span>Subtotal</span>
            <span>{formatPrice(subtotal)}</span>
          </div>
          <div className="summary-row total">
            <span>Total</span>
            <span>{formatPrice(subtotal)}</span>
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Placing Order...' : 'Place Order'}
          </button>
        </div>
      </form>
    </div>
  );
}
