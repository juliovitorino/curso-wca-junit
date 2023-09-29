package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Filme;

public class FilmeBuilder {

    private Filme filme;

    private FilmeBuilder() {}

    public static FilmeBuilder builder() {
        FilmeBuilder filmeBuilder = new FilmeBuilder();
        Filme filme = new Filme();
        filme.setNome("Filme para teste 1");
        filme.setEstoque(10);
        filme.setPrecoLocacao(4.0);

        filmeBuilder.filme = filme;
        return filmeBuilder;
    }

    public FilmeBuilder zerarEstoque() {
        filme.setEstoque(0);
        return this;
    }

    public Filme build() {
        return this.filme;
    }
}
