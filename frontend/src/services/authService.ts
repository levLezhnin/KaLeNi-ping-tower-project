import { apiAuth } from "./apiClient";

const TOKEN_KEY = "monitorpro_token";
const OWNER_KEY = "monitorpro_owner_id";
const EMAIL_KEY = "monitorpro_email";
const USERNAME_KEY = "monitorpro_username";

// RegisterPayload: username (обязателен), email (обязателен), password (обязателен)
export type RegisterPayload = { username: string; email: string; password: string };
export type LoginPayload = { email: string; password: string };

function getDefaultOwnerId(): string {
    return String(import.meta.env.VITE_OWNER_ID || "42");
}

export const authService = {
    isAuthenticated(): boolean {
        return Boolean(localStorage.getItem(TOKEN_KEY));
    },

    getOwnerId(): string {
        return localStorage.getItem(OWNER_KEY) || getDefaultOwnerId();
    },

    getEmail(): string | null {
        return localStorage.getItem(EMAIL_KEY);
    },

    getUsername(): string | null {
        return localStorage.getItem(USERNAME_KEY);
    },
    setUsername(username: string): void {
        localStorage.setItem(USERNAME_KEY, username);
    },

    async register(payload: RegisterPayload): Promise<void> {
        const { data } = await apiAuth.post("/api/auth/register", payload);
        if (data && data.id) {
            localStorage.setItem(TOKEN_KEY, "demo-token"); // или data.token если появится
            localStorage.setItem(OWNER_KEY, String(data.id));
            localStorage.setItem(EMAIL_KEY, data.email);
            localStorage.setItem(USERNAME_KEY, data.username || payload.username);
        } else {
            throw new Error("Регистрация не удалась: нет id пользователя");
        }
    },

    async getUserIdByEmail(email: string): Promise<number | null> {
        try {
            const { data } = await apiAuth.get(`/api/v1/users/email/${encodeURIComponent(email)}`);
            if (data && data.id) {
                localStorage.setItem(OWNER_KEY, String(data.id));
                return data.id;
            }
            return null;
        } catch {
            return null;
        }
    },

    async login(payload: LoginPayload): Promise<void> {
        // Реальный вход через /api/auth/signIn
        const { data: signInResult } = await apiAuth.post("/api/auth/signIn", payload);
        if (!signInResult) {
            throw new Error("Неверный email или пароль");
        }
        // Получить данные пользователя по email
        const { data: user } = await apiAuth.get(`/api/v1/users/email/${encodeURIComponent(payload.email)}`);
        if (user && user.id) {
            localStorage.setItem(TOKEN_KEY, "demo-token"); // или user.token если появится
            localStorage.setItem(OWNER_KEY, String(user.id));
            localStorage.setItem(EMAIL_KEY, user.email);
            localStorage.setItem(USERNAME_KEY, user.username);
        } else {
            throw new Error("Пользователь с таким email не найден");
        }
    },

    logout(): void {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(OWNER_KEY);
        localStorage.removeItem(EMAIL_KEY);
        localStorage.removeItem(USERNAME_KEY);
    },
};

export default authService;


