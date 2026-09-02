package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca;

import br.com.eventsbymc.eventsapi.domain.model.Perfil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) //essa anotação só pode ser usada em métodos
@Retention(RetentionPolicy.RUNTIME)//mantenha essa informação disponível em tempo de execução
public @interface RequerPerfil {
    Perfil[] value();
}
