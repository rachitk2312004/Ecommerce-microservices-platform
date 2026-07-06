import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div className="footer-brand">
          <strong>ShopEase</strong>
          <p>Your trusted e-commerce destination.</p>
        </div>
        <div className="footer-links">
          <Link to="/products">Shop</Link>
          <Link to="/login">Account</Link>
          <Link to="/admin">Admin</Link>
        </div>
        <p className="footer-copy">&copy; {new Date().getFullYear()} ShopEase. All rights reserved.</p>
      </div>
    </footer>
  );
}
