import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { logout } from '../api/auth';

export default function Layout() {
  const navigate = useNavigate();
  const businessName = localStorage.getItem('businessName') || 'My Business';

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <>
      <header className="app-header">
        <div className="app-brand">{businessName}</div>

        <nav className="app-nav">
          <NavLink to="/customers">Customers</NavLink>
          <NavLink to="/suppliers">Suppliers</NavLink>
          <NavLink to="/products">Products</NavLink>
          <NavLink to="/stock">Stock</NavLink>
          <NavLink to="/deliveries">Deliveries</NavLink>
          <NavLink to="/billing">Regular Billing</NavLink>
        </nav>

        <div className="header-actions">
          <NavLink to="/preferences">Preferences</NavLink>
          <button onClick={handleLogout}>Logout</button>
        </div>
      </header>

      <main className="app-main">
        <Outlet />
      </main>
    </>
  );
}
