import { Outlet, NavLink } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Footer from '../components/common/Footer';

const adminLinks = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/products', label: 'Products' },
  { to: '/admin/categories', label: 'Categories' },
  { to: '/admin/inventory', label: 'Inventory' },
];

export default function AdminLayout() {
  return (
    <div className="layout">
      <Navbar variant="admin" />
      <div className="admin-shell">
        <aside className="sidebar admin-sidebar">
          <h3>Admin Panel</h3>
          <nav className="sidebar-nav">
            {adminLinks.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.end}
                className={({ isActive }) =>
                  `sidebar-link${isActive ? ' active' : ''}`
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </aside>
        <main className="main-content admin-content">
          <Outlet />
        </main>
      </div>
      <Footer />
    </div>
  );
}
