import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { register } from "../api/auth";
import { errorMessage } from "../api/client";

export default function Register() {
    const [form, setForm] = useState({
        businessName: "",
        phoneNumber: "",
        email: "",
        password: "",
    });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    function update(field, value) {
        setForm({ ...form, [field]: value });
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
            await register(form.businessName, form.phoneNumber, form.email, form.password);
            navigate("/");
        } catch (err) {
            setError(errorMessage(err));
        } finally {
            setLoading(false);
        }
    }

    const box = { width: "100%", padding: 8, marginBottom: 12 };

    return (
        <div style={{ maxWidth: 360, margin: "80px auto", fontFamily: "sans-serif" }}>
            <h2>Create account</h2>
            <form onSubmit={handleSubmit}>
                <input placeholder="Business name" value={form.businessName}
                       onChange={(e) => update("businessName", e.target.value)} style={box} />
                <input placeholder="Phone number" value={form.phoneNumber}
                       onChange={(e) => update("phoneNumber", e.target.value)} style={box} />
                <input placeholder="Email (optional)" value={form.email}
                       onChange={(e) => update("email", e.target.value)} style={box} />
                <input type="password" placeholder="Password" value={form.password}
                       onChange={(e) => update("password", e.target.value)} style={box} />
                <small>8+ characters with uppercase, lowercase, a number and one of @#$%^&+=!</small>
                {error && <p style={{ color: "crimson" }}>{error}</p>}
                <div>
                    <button type="submit" disabled={loading} style={{ padding: "8px 16px", marginTop: 12 }}>
                        {loading ? "Creating..." : "Register"}
                    </button>
                </div>
            </form>
            <p>
                Already registered? <Link to="/login">Login</Link>
            </p>
        </div>
    );
}