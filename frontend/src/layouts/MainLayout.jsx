import { Outlet } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Footer from '../components/common/Footer';

export default function MainLayout() {
  return (
    <div className="layout">
      <Navbar variant="main" />
      <main className="main-content">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
