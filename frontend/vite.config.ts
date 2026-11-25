import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { detectarEnderecoRede } from './tools/endereco-presenca.cjs'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'API_')
  return {
    plugins: [
      ...(mode === 'desktop' ? [vue()] : [vue(), vueDevTools()]),
      {
        name: 'endereco-presenca-local',
        configureServer(server) {
          server.middlewares.use('/presenca-rede.json', async (req, res) => {
            res.setHeader('Cache-Control', 'no-store')
            if (!['GET', 'HEAD'].includes(req.method || '')) {
              res.statusCode = 405
              res.end()
              return
            }
            const endereco = await detectarEnderecoRede().catch(() => null)
            res.setHeader('Content-Type', 'application/json')
            res.end(req.method === 'HEAD' ? undefined : JSON.stringify({ endereco }))
          })
        },
      },
    ],
    resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
    server: {
      host: '0.0.0.0',
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
