// vite.config.ts
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
  port: 5173,
  open: false,
  proxy: {
    "/api": {
      target: "http://url-service_url_service_network:8080", 
      changeOrigin: true,
      secure: false,
    },
  },
}
});