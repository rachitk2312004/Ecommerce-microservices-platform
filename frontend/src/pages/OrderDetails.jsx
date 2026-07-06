import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { orderService } from '../services/orderService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import { formatPrice } from '../utils/currency';

export default function OrderDetails() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadOrder = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await orderService.getOrderById(id);
      setOrder(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrder();
  }, [id]);

  if (loading) return <LoadingSpinner fullPage />;
  if (error) return <div className="page"><ErrorMessage message={error} onRetry={loadOrder} /></div>;
  if (!order) return null;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Order {order.orderNumber}</h1>
        <Link to="/orders" className="btn btn-outline btn-sm">Back to Orders</Link>
      </div>

      <div className="order-detail-grid">
        <div className="detail-card">
          <h3>Order Info</h3>
          <div className="detail-row">
            <span className="detail-label">Status</span>
            <span className={`badge badge-${order.orderStatus?.toLowerCase()}`}>{order.orderStatus}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Payment</span>
            <span>{order.paymentStatus}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Total</span>
            <span className="product-price">{formatPrice(order.totalAmount)}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Placed</span>
            <span>{new Date(order.createdAt).toLocaleString()}</span>
          </div>
        </div>

        <div className="detail-card">
          <h3>Items</h3>
          <div className="table-wrapper">
            <table className="table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Qty</th>
                  <th>Unit Price</th>
                  <th>Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {(order.items || []).map((item) => (
                  <tr key={item.id}>
                    <td>{item.productName}</td>
                    <td>{item.quantity}</td>
                    <td>{formatPrice(item.unitPrice)}</td>
                    <td>{formatPrice(item.subtotal)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
