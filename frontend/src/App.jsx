import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import { logout } from "./api/auth";

// Like SecurityConfig's anyRequest().authenticated()
function ProtectedRoute({ children }) {
  return localStorage.getItem("token") ? children : <Navigate to="/login" />;
}

function Home() {
  return (
      <div style={{ fontFamily: "sans-serif", padding: 24 }}>
        <h2>Welcome, {localStorage.getItem("businessName")}</h2>
        <p>You are logged in. Customers page coming next.</p>
        <button onClick={() => { logout(); window.location.href = "/login"; }}>
          Logout
        </button>
      </div>
  );
}

export default function App() {
  return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/" element={<ProtectedRoute><Home /></ProtectedRoute>} />
      </Routes>
  );
}