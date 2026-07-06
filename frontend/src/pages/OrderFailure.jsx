import { Link, useLocation } from 'react-router-dom';

export default function OrderFailure() {
  const location = useLocation();
  const message = location.state?.message || 'Something went wrong while processing your order.';

  return (
    <div className="page result-page">
      <div className="result-card failure">
        <div className="result-icon">✕</div>
        <h1>Order Failed</h1>
        <p>{message}</p>
        <div className="result-actions">
          <Link to="/cart" className="btn btn-primary">
            Return to Cart
          </Link>
          <Link to="/products" className="btn btn-outline">
            Continue Shopping
          </Link>
        </div>
      </div>
    </div>
  );
}
