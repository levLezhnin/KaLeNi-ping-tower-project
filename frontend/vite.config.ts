// vite.config.ts
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0", // Важно! Позволяет принимать соединения извне контейнера
    port: 5173,
    open: false,
    proxy: {
      "/api": {
        target: "http://url_service:8080", // Исправлено имя контейнера
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
