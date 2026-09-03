package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

public class LoginDTO{

    private String email;
    private String senha;

    public LoginDTO(){

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
    //o Jackson nos obriga a montar o objeto "vazio" para que ele consiga popular os campos do objeto com os dados recebidos no JSON.
    //Jackson é uma lib Java que faz a conversão de JSON para objeto Java e vice-versa
}