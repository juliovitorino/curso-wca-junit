package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.utils.DataUtils;

import java.util.Arrays;
import java.util.Date;

import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;

public class LocacaoBuilder {

    private Locacao locacao;

    private LocacaoBuilder() {}

    public static LocacaoBuilder builder() {
        LocacaoBuilder locacaoBuilder = new LocacaoBuilder();
        Locacao locacao = new Locacao();
        locacao.setDataLocacao(new Date());
        locacao.setDataRetorno(obterDataComDiferencaDias(locacao.getDataLocacao(),1));
        locacao.setUsuario(UsuarioBuilder.builder().build());
        locacao.setFilme(Arrays.asList(FilmeBuilder.builder().build()));
        locacao.setValor(4.0);
        locacaoBuilder.locacao = locacao;
        return locacaoBuilder;
    }

    public LocacaoBuilder atrasado() {
        this.locacao.setDataLocacao(obterDataComDiferencaDias(-4));
        this.locacao.setDataRetorno(obterDataComDiferencaDias(-2));
        return this;
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
