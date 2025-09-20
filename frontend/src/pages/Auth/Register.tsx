import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authService } from "../../services/authService";

export default function Register() {
  const [email, setEmail] = useState("");
  const [pwd, setPwd] = useState("");
  const [pwd2, setPwd2] = useState("");
  const [errors, setErrors] = useState<string | null>(null);
  const navigate = useNavigate();

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const errs: string[] = [];
    if (!email.trim()) errs.push("Введите email");
    else if (!emailRe.test(email)) errs.push("Некорректный email");
    if (!pwd) errs.push("Введите пароль");
    if (!pwd2) errs.push("Подтвердите пароль");
    if (pwd && pwd2 && pwd !== pwd2) errs.push("Пароли не совпадают");
    if (errs.length) { setErrors(errs.join(". ")); return; }
    setErrors(null);
    await authService.register({ email, password: pwd });
    navigate("/dashboard");
  };

  return (
    <div className="max-w-md mx-auto my-16 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))] p-8 rounded-xl shadow">
      <h3 className="text-2xl font-semibold mb-4">Регистрация</h3>
      <form onSubmit={submit} className="space-y-4">
        {errors && <div className="text-red-600 text-sm">{errors}</div>}
        <label className="block">
          <span className="text-sm text-[hsl(var(--muted-foreground))]">Email</span>
          <input
            className="mt-1 block w-full border border-[hsl(var(--border))] rounded px-3 py-2 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>
        <label className="block">
          <span className="text-sm text-[hsl(var(--muted-foreground))]">Пароль</span>
          <input
            type="password"
            className="mt-1 block w-full border border-[hsl(var(--border))] rounded px-3 py-2 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
            value={pwd}
            onChange={(e) => setPwd(e.target.value)}
          />
        </label>
        <label className="block">
          <span className="text-sm text-[hsl(var(--muted-foreground))]">Подтверждение пароля</span>
          <input
            type="password"
            className="mt-1 block w-full border border-[hsl(var(--border))] rounded px-3 py-2 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
            value={pwd2}
            onChange={(e) => setPwd2(e.target.value)}
          />
        </label>
        <div className="flex items-center justify-between">
          <button className="bg-[hsl(var(--primary))] text-[hsl(var(--primary-foreground))] px-4 py-2 rounded">
            Создать аккаунт
          </button>
          <a className="text-sm text-[hsl(var(--muted-foreground))]" href="/login">
            Уже есть аккаунт?
          </a>
        </div>
      </form>
    </div>
  );
}