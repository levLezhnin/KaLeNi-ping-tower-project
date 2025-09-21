import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authService } from "../../services/authService";

export default function Register() {
  const [email, setEmail] = useState("");
  const [pwd, setPwd] = useState("");
  const [pwd2, setPwd2] = useState("");
  const [username, setUsername] = useState("");
  const [errors, setErrors] = useState<string | null>(null);
  const navigate = useNavigate();

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const errs: string[] = [];
    if (!username.trim()) errs.push("Введите имя пользователя");
    if (!email.trim()) errs.push("Введите email");
    else if (!emailRe.test(email)) errs.push("Некорректный email");
    if (!pwd) errs.push("Введите пароль");
    else {
      if (pwd.length <= 8) errs.push("Пароль должен быть длиннее 8 символов");
      if (!/[A-Z]/.test(pwd)) errs.push("Пароль должен содержать хотя бы одну заглавную букву");
      if (!/[0-9]/.test(pwd)) errs.push("Пароль должен содержать хотя бы одну цифру");
      if (!/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(pwd)) errs.push("Пароль должен содержать хотя бы один специальный символ");
      if (!/^[A-Za-z0-9!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]+$/.test(pwd)) errs.push("Пароль должен содержать только латинские буквы, цифры и спецсимволы");
    }
    if (!pwd2) errs.push("Подтвердите пароль");
    if (pwd && pwd2 && pwd !== pwd2) errs.push("Пароли не совпадают");
    if (errs.length) { setErrors(errs.join(". ")); return; }
    setErrors(null);
    await authService.register({ username, email, password: pwd });
    localStorage.setItem("show_telegram_modal", "1");
    navigate("/dashboard");
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-8 text-center">
            <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
              </svg>
            </div>
            <h1 className="text-2xl font-bold text-white">Создать аккаунт</h1>
            <p className="text-blue-100 mt-2">Присоединяйтесь к MonitorPro</p>
          </div>

          {/* Form */}
          <div className="p-6">
            <form onSubmit={submit} className="space-y-6">
              {errors && (
                <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span className="text-red-700 dark:text-red-400 text-sm font-medium">{errors}</span>
                  </div>
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Имя пользователя *
                </label>
                <input
                  className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                  placeholder="Введите имя пользователя"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Email *
                </label>
                <input
                  type="email"
                  className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                  placeholder="Введите email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Пароль *
                </label>
                <input
                  type="password"
                  className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                  placeholder="Создайте пароль"
                  value={pwd}
                  onChange={(e) => setPwd(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Подтвердите пароль *
                </label>
                <input
                  type="password"
                  className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                  placeholder="Повторите пароль"
                  value={pwd2}
                  onChange={(e) => setPwd2(e.target.value)}
                  required
                />
              </div>

              {/* Password Requirements */}
              {pwd && (
                <div className="p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
                  <h4 className="text-sm font-medium text-blue-900 dark:text-blue-300 mb-2">Требования к паролю:</h4>
                  <ul className="text-xs text-blue-800 dark:text-blue-400 space-y-1">
                    <li className={`flex items-center gap-2 ${pwd.length > 8 ? 'text-green-600' : ''}`}>
                      <svg className={`w-3 h-3 ${pwd.length > 8 ? 'text-green-500' : 'text-gray-400'}`} fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      Длина более 8 символов
                    </li>
                    <li className={`flex items-center gap-2 ${/[A-Z]/.test(pwd) ? 'text-green-600' : ''}`}>
                      <svg className={`w-3 h-3 ${/[A-Z]/.test(pwd) ? 'text-green-500' : 'text-gray-400'}`} fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      Заглавная буква
                    </li>
                    <li className={`flex items-center gap-2 ${/[0-9]/.test(pwd) ? 'text-green-600' : ''}`}>
                      <svg className={`w-3 h-3 ${/[0-9]/.test(pwd) ? 'text-green-500' : 'text-gray-400'}`} fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      Цифра
                    </li>
                    <li className={`flex items-center gap-2 ${/[!@#$%^&*()_+\-=[\]{};':\"\\|,.<>/?]/.test(pwd) ? 'text-green-600' : ''}`}>
                      <svg className={`w-3 h-3 ${/[!@#$%^&*()_+\-=[\]{};':\"\\|,.<>/?]/.test(pwd) ? 'text-green-500' : 'text-gray-400'}`} fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      Специальный символ
                    </li>
                  </ul>
                </div>
              )}

              <button
                type="submit"
                className="w-full bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              >
                Создать аккаунт
              </button>

              <div className="text-center">
                <span className="text-sm text-gray-600 dark:text-gray-400">Уже есть аккаунт? </span>
                <a
                  href="/login"
                  className="text-sm font-medium text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 transition-colors"
                >
                  Войти
                </a>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}