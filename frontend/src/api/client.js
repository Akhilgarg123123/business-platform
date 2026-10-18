import axios from "axios";

const client = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
});

// Runs before EVERY request: attaches the token, like JwtAuthFilter in reverse
client.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Runs after EVERY response: if the token is rejected, send the user to login
client.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;
        const isAuthCall = error.config?.url?.startsWith("/auth/");
        if ((status === 401 || status === 403) && !isAuthCall) {
            localStorage.removeItem("token");
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);

// Turns any backend error into one readable message
export function errorMessage(error) {
    const data = error.response?.data;
    if (data?.errors) return Object.values(data.errors).join(". "); // validation errors
    if (data?.message) return data.message;
    return "Could not reach the server";
}

export default client;