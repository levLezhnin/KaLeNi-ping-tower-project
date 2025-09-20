import { useState } from "react";
import { authService } from "../../services/authService";
import api from "../../services/apiClient";

export default function Profile() {
    const [username, setUsername] = useState(authService.getUsername() || "");
    const [password, setPassword] = useState("");
    const [password2, setPassword2] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    const userId = authService.getOwnerId();
    const email = authService.getEmail();

    const validatePassword = (pwd: string) => {
        if (pwd.length <= 8) return "Пароль должен быть длиннее 8 символов";
        if (!/[A-Z]/.test(pwd)) return "Пароль должен содержать хотя бы одну заглавную букву";
        if (!/[0-9]/.test(pwd)) return "Пароль должен содержать хотя бы одну цифру";
        if (!/[!@#$%^&*()_+\-=[\]{};':\"\\|,.<>/?]/.test(pwd)) return "Пароль должен содержать хотя бы один специальный символ";
        if (!/^[A-Za-z0-9!@#$%^&*()_+\-=[\]{};':\"\\|,.<>/?]+$/.test(pwd)) return "Пароль должен содержать только латинские буквы, цифры и спецсимволы";
        return null;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        if (!username.trim()) {
            setError("Имя пользователя обязательно");
            return;
        }
        if (password) {
            const pwdErr = validatePassword(password);
            if (pwdErr) {
                setError(pwdErr);
                return;
            }
            if (password !== password2) {
                setError("Пароли не совпадают");
                return;
            }
        }
        setLoading(true);
        try {
            const payload: any = { username };
            if (password) payload.password = password;
            const { data } = await api.put(`/api/v1/users/update/${userId}`, payload);
            if (data && data.id) {
                authService.setUsername(data.username);
                setSuccess("Данные успешно обновлены");
                setPassword("");
                setPassword2("");
            } else {
                setError("Не удалось обновить данные пользователя");
            }
        } catch (e: any) {
            setError(e?.response?.data?.message || e?.response?.data?.error || e.message || "Ошибка обновления");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-md mx-auto my-16 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))] p-8 rounded-xl shadow">
            <h3 className="text-2xl font-semibold mb-4">Личный кабинет</h3>
            <div className="mb-4 text-sm text-gray-500">Email: <span className="font-mono">{email}</span></div>
            <form onSubmit={handleSubmit} className="space-y-4">
                {error && <div className="text-red-600 text-sm">{error}</div>}
                {success && <div className="text-green-600 text-sm">{success}</div>}
                <label className="block">
                    <span className="text-sm text-[hsl(var(--muted-foreground))]">Имя пользователя</span>
                    <input
                        className="mt-1 block w-full border border-[hsl(var(--border))] rounded px-3 py-2 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                    />
                </label>
                <label className="block">
                    <span className="text-sm text-[hsl(var(--muted-foreground))]">Новый пароль (необязательно)</span>
                    <input
                        type="password"
                        className="mt-1 block w-full border border-[hsl(var(--border))] rounded px-3 py-2 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                    />
                </label>
                <label className="block">
                    <span className="text-sm text-[hsl(var(--muted-foreground))]">Подтвердите новый пароль</span>
                    <input
                        type="password"
                        className="mt-1 block w-full border border-[hsl(var(--border))] rounded px-3 py-2 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
                        value={password2}
                        onChange={e => setPassword2(e.target.value)}
                    />
                </label>
                <button
                    type="submit"
                    className="w-full bg-[hsl(var(--primary))] text-[hsl(var(--primary-foreground))] px-4 py-2 rounded mt-2 disabled:opacity-60"
                    disabled={loading}
                >
                    {loading ? "Сохраняем..." : "Сохранить изменения"}
                </button>
            </form>
        </div>
    );
}
