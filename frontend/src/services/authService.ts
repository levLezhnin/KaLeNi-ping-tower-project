const TOKEN_KEY = "monitorpro_token";
const OWNER_KEY = "monitorpro_owner_id";
const EMAIL_KEY = "monitorpro_email";

export type RegisterPayload = { email: string; password: string };
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

    async register(payload: RegisterPayload): Promise<void> {
        // No backend auth endpoints provided. Emulate success.
        localStorage.setItem(TOKEN_KEY, "demo-token");
        // Derive an owner id deterministically from email (fallback to env default)
        const owner = this.deriveOwnerId(payload.email);
        localStorage.setItem(OWNER_KEY, owner);
        localStorage.setItem(EMAIL_KEY, payload.email);
    },

    async login(payload: LoginPayload): Promise<void> {
        // Emulate login; in future, call backend and set token/owner id from response
        localStorage.setItem(TOKEN_KEY, "demo-token");
        const owner = this.deriveOwnerId(payload.email);
        localStorage.setItem(OWNER_KEY, owner);
        localStorage.setItem(EMAIL_KEY, payload.email);
    },

    logout(): void {
        localStorage.removeItem(TOKEN_KEY);
        // Keep OWNER_ID? For now, clear to force explicit choice next time
        localStorage.removeItem(OWNER_KEY);
        localStorage.removeItem(EMAIL_KEY);
    },

    deriveOwnerId(email: string): string {
        // Very simple stable hash to numeric string in [1..1e6]
        let h = 0;
        for (let i = 0; i < email.length; i++) {
            h = (h * 31 + email.charCodeAt(i)) >>> 0;
        }
        const id = (h % 1000000) + 1;
        return String(id);
    },
};

export default authService;


