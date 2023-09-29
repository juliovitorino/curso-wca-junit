package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;

import java.util.Arrays;
import java.util.Date;

public class LocacaoBuilder {

    private Locacao locacao;

    private LocacaoBuilder() {}

    public static LocacaoBuilder builder() {
        LocacaoBuilder locacaoBuilder = new LocacaoBuilder();
        Locacao locacao = new Locacao();
        locacao.setDataLocacao(new Date());
        locacao.setUsuario(UsuarioBuilder.builder().build());
        locacao.setFilme(Arrays.asList(FilmeBuilder.builder().build()));
        locacaoBuilder.locacao = locacao;
        return locacaoBuilder;
    }

    public LocacaoBuilder comUsuario(Usuario usuario) {
        this.locacao.setUsuario(usuario);
        return this;
    }

    public LocacaoBuilder comDataDeRetorno(Date dataRetorno) {
        this.locacao.setDataRetorno(dataRetorno);
        return this;
    }
    public Locacao build() {
        return this.locacao;
    }
}
