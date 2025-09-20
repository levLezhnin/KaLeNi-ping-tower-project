import { lazy, type JSX } from "react";
import { createBrowserRouter, Navigate } from "react-router-dom";
import Layout from "../layouts/Layout";
import { authService } from "../services/authService";

const Home = lazy(() => import("../pages/Home/Home"));
const Login = lazy(() => import("../pages/Auth/Login"));
const Register = lazy(() => import("../pages/Auth/Register"));
const Dashboard = lazy(() => import("../pages/Dashboard/Dashboard"));
const UrlDetails = lazy(() => import("../pages/UrlDetails/UrlDetails"));
const Profile = lazy(() => import("../pages/Profile/Profile"));

function Protected({ children }: { children: JSX.Element }) {
  return authService.isAuthenticated() ? children : <Navigate to="/register" replace />;
}

export const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />,
    children: [
      { index: true, element: <Home /> },
      { path: "login", element: <Login /> },
      { path: "register", element: <Register /> },
      {
        path: "dashboard", element: (
          <Protected>
            <Dashboard />
          </Protected>
        )
      },
      { path: "url/:id", element: <UrlDetails /> },
      {
        path: "profile", element: (
          <Protected>
            <Profile />
          </Protected>
        )
      },
    ],
  },
]);

export default router;