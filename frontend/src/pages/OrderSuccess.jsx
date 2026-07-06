import { Link, useParams, useLocation } from 'react-router-dom';
import { formatPrice } from '../utils/currency';

export default function OrderSuccess() {
  const { orderId } = useParams();
  const location = useLocation();
  const order = location.state?.order;

  return (
    <div className="page result-page">
      <div className="result-card success">
        <div className="result-icon">✓</div>
        <h1>Order Placed Successfully!</h1>
        <p>Thank you for your purchase. Your order has been confirmed.</p>
        {order && (
          <div className="result-details">
            <p><strong>Order Number:</strong> {order.orderNumber}</p>
            <p><strong>Total:</strong> {formatPrice(order.totalAmount)}</p>
            <p><strong>Status:</strong> {order.orderStatus}</p>
          </div>
        )}
        {orderId && !order && (
          <p>Order ID: {orderId}</p>
        )}
        <div className="result-actions">
          <Link to={`/orders/${orderId || order?.id}`} className="btn btn-primary">
            View Order Details
          </Link>
          <Link to="/products" className="btn btn-outline">
            Continue Shopping
          </Link>
        </div>
      </div>
    </div>
  );
}
