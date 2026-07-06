import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Profile() {
  const { user } = useAuth();

  if (!user) return null;

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Profile</h1>
        <div className="page-actions">
          <Link to="/profile/edit" className="btn btn-outline btn-sm">Edit Profile</Link>
          <Link to="/profile/change-password" className="btn btn-outline btn-sm">Change Password</Link>
        </div>
      </div>

      <div className="profile-card">
        <div className="profile-avatar">
          {user.firstName?.charAt(0)}{user.lastName?.charAt(0)}
        </div>
        <div className="profile-details">
          <div className="detail-row">
            <span className="detail-label">Name</span>
            <span>{user.firstName} {user.lastName}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Email</span>
            <span>{user.email}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Phone</span>
            <span>{user.phone || '—'}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Address</span>
            <span>{user.address || '—'}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Role</span>
            <span className="badge">{user.role}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Member Since</span>
            <span>{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '—'}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
