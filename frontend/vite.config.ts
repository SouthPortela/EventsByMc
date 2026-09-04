import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // Em dev, o navegador chama /api/* no próprio localhost (sem CORS), e o
    // servidor Node do Vite repassa pro backend real, removendo o prefixo /api
    // — o mesmo comportamento que o Apache faz em produção (ProxyPass /api/ ...).
    proxy: {
      '/api': {
        target: 'https://eventos.monkeycorp.com.br',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
