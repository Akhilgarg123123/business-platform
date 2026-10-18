import client from "./client";

export async function login(phoneNumber, password) {
    const res = await client.post("/auth/login", { phoneNumber, password });
    localStorage.setItem("token", res.data.token);
    localStorage.setItem("businessName", res.data.businessName);
    return res.data;
}

export async function register(businessName, phoneNumber, email, password) {
    const res = await client.post("/auth/register", {
        businessName,
        phoneNumber,
        email: email || null,
        password,
    });
    localStorage.setItem("token", res.data.token);
    localStorage.setItem("businessName", res.data.businessName);
    return res.data;
}

export function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("businessName");
}