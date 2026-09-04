package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.port.in.AutenticarUsuario;
import br.com.eventsbymc.eventsapi.application.usecase.ResultadoAutenticacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AutenticacaoControlador {

    private final AutenticarUsuario autenticarUsuario;

    public AutenticacaoControlador(AutenticarUsuario autenticarUsuario) {
        this.autenticarUsuario = autenticarUsuario;
    }
   // Request + Post entregam o caminho completo 
   // o mesmo que o @RequestMapping("/auth") + @PostMapping("/login") = /auth/login
   @PostMapping("/login")
    public ResponseEntity<LoginRespostaDTO> autenticar(@RequestBody LoginDTO loginDTO) {
        ResultadoAutenticacao resultado = autenticarUsuario.autenticar(loginDTO.getEmail(), loginDTO.getSenha());
        return ResponseEntity.ok(LoginRespostaDTO.from(resultado));
    }
    //Contructor recebe AutenticarUsuario que é a interface
    // recebe email e senha do front e chama o usecase AutenticarUsuarioUseCase que implementa a interface AutenticarUsuario
}