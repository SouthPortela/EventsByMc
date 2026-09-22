import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import './assets/theme.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { pinia } from './stores'
import { useAuthStore } from './stores/auth'
import { configurarHttpClient } from './shared/services/httpClient'

const app = createApp(App)
app.use(pinia)

const auth = useAuthStore(pinia)
configurarHttpClient({
  obterToken: () => auth.token,
  aoNaoAutorizado: () => {
    auth.sair()

    const rotaAtual = router.currentRoute.value

    if (rotaAtual.name !== 'login') {
      void router.replace({
        name: 'login',
        query: {
          redirect: rotaAtual.fullPath,
        },
      })
    }
  },
})

app.use(router)
app.mount('#app')
