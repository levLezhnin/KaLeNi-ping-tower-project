// vite.config.ts
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: 5173,
    open: false,
    proxy: {
      "/api/auth": {
        target: "http://user-service:8081",
        changeOrigin: true,
        secure: false,
      },
      "/api/v1/users": {
        target: "http://user-service:8081",
        changeOrigin: true,
        secure: false,
      },
      "/api/v1/telegramNotifications": {
        target: "http://user-service:8083",
        changeOrigin: true,
        secure: false,
      },
      "/api": {
        target: "http://url-service:8080",
        changeOrigin: true,
        secure: false,
        bypass: (req) => {
          if (req.url.startsWith("/api/auth") || req.url.startsWith("/api/v1/users") || req.url.startsWith("/api/v1/telegramNotifications")) {
            return req.url;
          }
        },
      },
    },
  },
});
