import { Outlet, NavLink } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Footer from '../components/common/Footer';

const customerLinks = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/orders', label: 'Orders' },
  { to: '/notifications', label: 'Notifications' },
  { to: '/profile', label: 'Profile' },
  { to: '/cart', label: 'Cart' },
];

export default function CustomerLayout() {
  return (
    <div className="layout">
      <Navbar variant="customer" />
      <div className="customer-shell">
        <aside className="sidebar">
          <h3>My Account</h3>
          <nav className="sidebar-nav">
            {customerLinks.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `sidebar-link${isActive ? ' active' : ''}`
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </aside>
        <main className="main-content customer-content">
          <Outlet />
        </main>
      </div>
      <Footer />
    </div>
  );
}
