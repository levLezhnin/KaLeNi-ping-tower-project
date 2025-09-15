import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Register() {
  const [email, setEmail] = useState("");
  const [pwd, setPwd] = useState("");
  const navigate = useNavigate();

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    localStorage.setItem("monitorpro_token", "demo-token");
    navigate("/dashboard");
  };

  return (
    <div className="max-w-md mx-auto my-16 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))] p-8 rounded-xl shadow">
      <h3 className="text-2xl font-semibold mb-4">Регистрация</h3>
      <form onSubmit={submit} className="space-y-4">
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