import axios from "axios";

const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const defaultOwnerId = import.meta.env.VITE_OWNER_ID || "42";

export const api = axios.create({
    baseURL,
});

api.interceptors.request.use((config) => {
    // Temporary owner header until auth is implemented
    const ownerId = localStorage.getItem("monitorpro_owner_id") || String(defaultOwnerId);
    config.headers = config.headers || {};
    (config.headers as Record<string, string>)["X-Owner-Id"] = ownerId;
    const token = localStorage.getItem("monitorpro_token");
    if (token) {
        (config.headers as Record<string, string>)["Authorization"] = `Bearer ${token}`;
    }
    return config;
});

export default api;


