import { Link, useNavigate, useLocation } from "react-router-dom";
import { authService } from "../services/authService";

export default function Header() {
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { name: "Главная", path: "/" },
    { name: "Дашборд", path: "/dashboard" },
  ];

  return (
    <header className="bg-white border-b border-transparent shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-4">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-blue-500 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">M</span>
            </div>
            <span className="text-xl font-semibold text-gray-900">MonitorPro</span>
          </div>

          <nav className="hidden md:flex space-x-6">
            {navItems.map((item) => {
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.name}
                  to={item.path}
                  className={`font-medium ${isActive ? "text-green-600" : "text-gray-600 hover:text-gray-900"
                    }`}
                >
                  {item.name}
                </Link>
              );
            })}
          </nav>

          <div className="flex items-center space-x-3">
            {authService.isAuthenticated() ? (
              <>
                {(authService.getUsername() || authService.getEmail()) && (
                  <div className="flex flex-col items-end mr-2">
                    <div className="flex items-center gap-2">
                      {authService.getUsername() && (
                        <span className="font-semibold text-base text-gray-900 leading-tight">
                          {authService.getUsername()}
                        </span>
                      )}
                      <Link
                        to="/profile"
                        className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-gray-200 hover:bg-gray-300 text-gray-700 ml-1"
                        title="Личный кабинет"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.5 20.25a8.25 8.25 0 1115 0v.75a.75.75 0 01-.75.75h-13.5a.75.75 0 01-.75-.75v-.75z" />
                        </svg>
                      </Link>
                    </div>
                    {authService.getEmail() && (
                      <span className="text-xs text-gray-500 leading-tight">
                        {authService.getEmail()}
                      </span>
                    )}
                  </div>
                )}
                <button
                  onClick={() => { authService.logout(); navigate("/"); }}
                  className="bg-gray-100 text-gray-900 px-4 py-2 rounded-lg hover:bg-gray-200 transition-colors"
                >
                  Выйти
                </button>
              </>
            ) : (
              <button
                onClick={() => navigate("/register")}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                Регистрация
              </button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}