import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import Customers from './pages/Customers';
import Suppliers from './pages/Suppliers';
import Products from './pages/Products';
import Stock from './pages/Stock';
import Deliveries from './pages/Deliveries';
import Billing from './pages/Billing';
import Preferences from './pages/Preferences';

// Only lets you in if there is a saved token (the backend still checks it on every request).
function RequireAuth({ children }) {
  return localStorage.getItem('token') ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route path="/customers" element={<Customers />} />
        <Route path="/suppliers" element={<Suppliers />} />
        <Route path="/products" element={<Products />} />
        <Route path="/stock" element={<Stock />} />
        <Route path="/deliveries" element={<Deliveries />} />
        <Route path="/billing" element={<Billing />} />
        <Route path="/preferences" element={<Preferences />} />
      </Route>

      <Route path="*" element={<Navigate to="/customers" replace />} />
    </Routes>
  );
}
