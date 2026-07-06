import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { notificationService } from '../services/notificationService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';

export default function Notifications() {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadNotifications = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await notificationService.getByUserId(user.id);
      setNotifications(data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.id) loadNotifications();
  }, [user?.id]);

  return (
    <div className="page">
      <div className="page-header">
        <h1>Notifications</h1>
      </div>

      {loading && <LoadingSpinner />}
      <ErrorMessage message={error} onRetry={loadNotifications} />

      {!loading && !error && (
        notifications.length === 0 ? (
          <p className="empty-state">No notifications yet.</p>
        ) : (
          <ul className="notification-list full">
            {notifications.map((n) => (
              <li key={n.id} className="notification-item">
                <div className="notification-header">
                  <strong>{n.title}</strong>
                  <span className="badge">{n.type}</span>
                </div>
                <p>{n.message}</p>
                <small>{new Date(n.createdAt).toLocaleString()}</small>
              </li>
            ))}
          </ul>
        )
      )}
    </div>
  );
}
