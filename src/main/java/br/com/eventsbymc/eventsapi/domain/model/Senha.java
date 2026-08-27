package br.com.eventsbymc.eventsapi.domain.model;

public class Senha {
    
    private final String hash;

    public Senha(String hash) {
        if(hash == null || hash.isBlank()){
        throw new IllegalArgumentException("O hash da senha é obrigatório.");
    }
        this.hash = hash;
    }
   
    public String getHash() {
        return hash;
    }
}