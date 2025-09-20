import { Link } from "react-router-dom";
import { authService } from "../../services/authService";

export default function Home() {
  return (
    <div className="min-h-[70vh] bg-gray-50 text-gray-900">
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-5xl md:text-6xl font-light text-gray-600 mb-2">
            Мониторинг сайтов
          </h1>
          <h2 className="text-4xl md:text-5xl font-bold mb-6">
            на <span className="bg-gradient-to-r from-green-500 to-blue-500 bg-clip-text text-transparent">MonitorPro</span>
          </h2>
          <p className="text-lg text-gray-600 mb-8 max-w-3xl mx-auto">
            24-часовой мониторинг доступности веб-сайтов
          </p>

          <div className="flex justify-center gap-4">
            {authService.isAuthenticated() ? (
              <Link
                to="/dashboard"
                className="bg-blue-600 text-white px-6 py-3 rounded-lg shadow hover:bg-blue-700"
              >
                Перейти в дашборд
              </Link>
            ) : (
              <>
                <Link
                  to="/register"
                  className="bg-blue-600 text-white px-6 py-3 rounded-lg shadow hover:bg-blue-700"
                >
                  Регистрация
                </Link>
                <Link
                  to="/login"
                  className="bg-gray-200 text-blue-700 px-6 py-3 rounded-lg shadow hover:bg-gray-300 border border-blue-600"
                >
                  Вход
                </Link>
              </>
            )}
          </div>
        </div>
      </section>

      <section className="py-12 bg-gray-100">
        <div className="max-w-7xl mx-auto px-4">
          <h3 className="text-2xl font-bold mb-6 text-gray-900">Возможности</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-white p-6 rounded-xl shadow-sm text-gray-900">
              <h4 className="font-semibold mb-2">Цветовая индикация статуса</h4>
              <p className="text-sm text-gray-600">
                Мгновенно видите состояние всех ваших сайтов.
              </p>
            </div>
            <div className="bg-white p-6 rounded-xl shadow-sm text-gray-900">
              <h4 className="font-semibold mb-2">Графики доступности</h4>
              <p className="text-sm text-gray-600">
                Детальная статистика за день, месяц, год и все время.
              </p>
            </div>
            <div className="bg-white p-6 rounded-xl shadow-sm text-gray-900">
              <h4 className="font-semibold mb-2">Мгновенные уведомления</h4>
              <p className="text-sm text-gray-600">
                Получайте уведомления о сбоях в режиме реального времени.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}