import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'API_')
  return {
    plugins: [vue(), vueDevTools()],
    resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
    server: {
      proxy: {
        '/api': {
          target: env.API_PROXY_TARGET || 'http://127.0.0.1:8080',
          changeOrigin: true,
          // O Java local não tem /api. Um proxy remoto pode exigir esse prefixo.
          rewrite: (path) =>
            env.API_PROXY_KEEP_PREFIX === 'true' ? path : path.replace(/^\/api/, ''),
        },
      },
    },
  }
})
