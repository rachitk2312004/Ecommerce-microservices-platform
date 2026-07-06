import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderService } from '../services/orderService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import ConfirmDialog from '../components/common/ConfirmDialog';
import { useToast } from '../context/ToastContext';
import { formatPrice } from '../utils/currency';

export default function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelId, setCancelId] = useState(null);
  const { showToast } = useToast();

  const loadOrders = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await orderService.getMyOrders();
      setOrders(data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const handleCancel = async () => {
    if (!cancelId) return;
    try {
      await orderService.cancelOrder(cancelId);
      showToast('Order cancelled');
      setCancelId(null);
      loadOrders();
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  const canCancel = (status) => ['PENDING', 'CONFIRMED'].includes(status);

  return (
    <div className="page">
      <div className="page-header">
        <h1>Order History</h1>
      </div>

      {loading && <LoadingSpinner />}
      <ErrorMessage message={error} onRetry={loadOrders} />

      {!loading && !error && (
        orders.length === 0 ? (
          <p className="empty-state">No orders yet. <Link to="/products">Start shopping</Link></p>
        ) : (
          <div className="table-wrapper">
            <table className="table">
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Status</th>
                  <th>Payment</th>
                  <th>Total</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id}>
                    <td>
                      <Link to={`/orders/${order.id}`}>{order.orderNumber}</Link>
                    </td>
                    <td><span className={`badge badge-${order.orderStatus?.toLowerCase()}`}>{order.orderStatus}</span></td>
                    <td>{order.paymentStatus}</td>
                    <td>{formatPrice(order.totalAmount)}</td>
                    <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td>
                      <Link to={`/orders/${order.id}`} className="btn btn-outline btn-sm">View</Link>
                      {canCancel(order.orderStatus) && (
                        <button type="button" className="btn btn-danger btn-sm" onClick={() => setCancelId(order.id)}>
                          Cancel
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}

      <ConfirmDialog
        isOpen={Boolean(cancelId)}
        title="Cancel Order"
        message="Are you sure you want to cancel this order?"
        confirmLabel="Cancel Order"
        onConfirm={handleCancel}
        onCancel={() => setCancelId(null)}
        danger
      />
    </div>
  );
}
