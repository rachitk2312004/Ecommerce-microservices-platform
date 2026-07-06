import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { orderService } from '../services/orderService';
import { notificationService } from '../services/notificationService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import { formatPrice } from '../utils/currency';

export default function Dashboard() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [orderData, notifData] = await Promise.all([
        orderService.getMyOrders(),
        notificationService.getByUserId(user.id),
      ]);
      setOrders(orderData || []);
      setNotifications((notifData || []).slice(0, 5));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.id) loadData();
  }, [user?.id]);

  const recentOrders = orders.slice(0, 3);

  return (
    <div className="page">
      <div className="page-header">
        <h1>Welcome, {user?.firstName}!</h1>
        <p>Manage your orders and account from here.</p>
      </div>

      <div className="dashboard-cards">
        <div className="stat-card">
          <span className="stat-label">Total Orders</span>
          <span className="stat-value">{orders.length}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Notifications</span>
          <span className="stat-value">{notifications.length}</span>
        </div>
        <Link to="/cart" className="stat-card stat-card-link">
          <span className="stat-label">Quick Action</span>
          <span className="stat-value">View Cart</span>
        </Link>
      </div>

      {loading && <LoadingSpinner />}
      <ErrorMessage message={error} onRetry={loadData} />

      {!loading && !error && (
        <>
          <section className="dashboard-section">
            <div className="section-header">
              <h2>Recent Orders</h2>
              <Link to="/orders">View all</Link>
            </div>
            {recentOrders.length === 0 ? (
              <p className="empty-state">No orders yet. <Link to="/products">Start shopping</Link></p>
            ) : (
              <div className="table-wrapper">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Order #</th>
                      <th>Status</th>
                      <th>Total</th>
                      <th>Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentOrders.map((order) => (
                      <tr key={order.id}>
                        <td>
                          <Link to={`/orders/${order.id}`}>{order.orderNumber}</Link>
                        </td>
                        <td><span className={`badge badge-${order.orderStatus?.toLowerCase()}`}>{order.orderStatus}</span></td>
                        <td>{formatPrice(order.totalAmount)}</td>
                        <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section className="dashboard-section">
            <div className="section-header">
              <h2>Recent Notifications</h2>
              <Link to="/notifications">View all</Link>
            </div>
            {notifications.length === 0 ? (
              <p className="empty-state">No notifications.</p>
            ) : (
              <ul className="notification-list">
                {notifications.map((n) => (
                  <li key={n.id} className="notification-item">
                    <strong>{n.title}</strong>
                    <p>{n.message}</p>
                    <small>{new Date(n.createdAt).toLocaleString()}</small>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </div>
  );
}
