package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Usuario;

public class UsuarioBuilder {

    private Usuario usuario;
    private UsuarioBuilder() {}

    public static UsuarioBuilder builder() {
        UsuarioBuilder usuarioBuilder = new UsuarioBuilder();
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario de teste 1");
        usuarioBuilder.usuario = usuario;
        return usuarioBuilder;
    }

    public UsuarioBuilder comNome(String nome) {
        this.usuario.setNome(nome);
        return this;
    }

    public Usuario build() {
        return usuario;
    }


}
