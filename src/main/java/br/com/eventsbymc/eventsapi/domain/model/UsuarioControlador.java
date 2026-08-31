package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.usecase.RegistrarUsuarioUseCase;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

// O Controller é o "Recepcionista" da internet
@RestController
@RequestMapping("/usuarios")
public class UsuarioControlador {

    // Declara o atributo do nosso "Maestro" aqui:
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    // construtor para o Spring injetar o Maestro:
    public UsuarioControlador(RegistrarUsuarioUseCase registrarUsuarioUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
    }

    // O método fica solto direto na classe:
    @PostMapping
    public ResponseEntity<String> registrarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        
        registrarUsuarioUseCase.registrarUsuario(usuarioDTO.getNome(), usuarioDTO.getEmail(), usuarioDTO.getSenha());
        
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }
}