   package br.com.eventsbymc.eventsapi.infrastructure.adapter.out.persistence;
   import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
   import br.com.eventsbymc.eventsapi.application.usecase.RegistrarUsuarioUseCase;


@Entity
   public class UsuarioEntidade {
        private String nome;
        private String email;
        private String senha;

        public UsuarioEntidade(String nome, String email, String senha) {
            this.nome = nome;
            this.email = email;
            this.senha = senha;
        }

        // Getters e setters
        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSenha() {
            return senha;
        }

        public void setSenha(String senha) {
            this.senha = senha;
        }
    
    }