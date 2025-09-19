import axios from "axios";

const BACKEND_URL = "/";
const DEFAULT_OWNER_ID = "42";

export const api = axios.create({
    baseURL: BACKEND_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use((config) => {
    const ownerId = DEFAULT_OWNER_ID;
    config.headers = config.headers || {};
    (config.headers as Record<string, any>)["X-Owner-Id"] = parseInt(ownerId, 10);

    const token = localStorage.getItem("monitorpro_token");
    if (token) {
        (config.headers as Record<string, string>)["Authorization"] = `Bearer ${token}`;
    }

    return config;
});

export default api;