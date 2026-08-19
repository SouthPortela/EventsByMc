package br.com.eventsbymc.eventsapi.domain.model.evento;

import org.springframework.web.multipart.MultipartFile;

public record EventoRequestDTO(String nome, String estado, String descricao, String imgUrl, String eventoUrl ,Long data, MultipartFile image) {
}
