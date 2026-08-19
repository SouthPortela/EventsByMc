package br.com.eventsbymc.eventsapi.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import br.com.eventsbymc.eventsapi.domain.model.evento.Evento;
import br.com.eventsbymc.eventsapi.domain.model.evento.EventoRequestDTO;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Objects;
import java.util.UUID;

@Service
public class EventoService {

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public Evento criarEvento(EventoRequestDTO dado) {
        String imgUrl = null;

        if (dado.image() != null) {
            imgUrl = this.uploadImg(dado.image());
        }

        Evento novoEvento = new Evento();
        novoEvento.setNome(dado.nome());
        novoEvento.setDescricao(dado.descricao());
        novoEvento.setEventoUrl(dado.eventoUrl());
        novoEvento.setData(new Date(dado.data()));
        novoEvento.setImgUrl(imgUrl);

        return novoEvento;
    }

    private String uploadImg(MultipartFile arq) {
        String imgNome = UUID.randomUUID() + "-" + arq.getOriginalFilename();

        try {
            File file = this.convertMultipartToFile(arq);
            s3Client.putObject(bucketName ,imgNome ,file);
            file.delete();
            return s3Client.getUrl(bucketName, imgNome).toString();
        } catch (Exception e) {
            System.out.println("Erro ao subir o arquivo");
            return null;
        }
    }

    private File convertMultipartToFile (MultipartFile arq) throws IOException {
        File convFile = new File(Objects.requireNonNull(arq.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(arq.getBytes());
        fos.close();
        return convFile;
    }
}
