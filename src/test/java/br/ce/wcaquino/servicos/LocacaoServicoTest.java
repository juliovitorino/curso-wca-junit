package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exception.FilmeSemEstoqueException;
import br.ce.wcaquino.exception.LocadoraException;
import br.ce.wcaquino.matchers.DiaSemanaMatcher;
import br.ce.wcaquino.matchers.MatchersProprios;
import br.ce.wcaquino.utils.DataUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LocacaoServicoTest {

    private LocacaoService locacaoService;
    private Usuario usuario;
    private List<Filme> filmes;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        locacaoService = new LocacaoService();
        usuario = new Usuario("Fulano");
        filmes = new ArrayList<Filme>();
    }

    @After
    public void tearDown() {
        System.out.println("After executado depois do método unitário encerrado");
    }

    @BeforeClass
    public static void setupClass() {
        System.out.println("before class executado");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("After class executado");
    }

    @Test
    public void deveDevolverFilmeNaSegundaAoAlugarNoSabado() {

        Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
        // cenario
        filmes.add(new Filme("The big short",10,5.0));

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

        //validação
        boolean ehSegunda = DataUtils.verificarDiaSemana(locacao.getDataRetorno(),Calendar.MONDAY);
        Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiEm(Calendar.MONDAY));
        Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiNumaSegunda());
        Assert.assertTrue(ehSegunda);

    }

    @Test
    public void alugarFilmeComSucesso() {
        // cenario
        Date agora = new Date();
        Date expectedDateLocacaoHoje = new Date();
        Date expectedDateEntrega = DataUtils.obterDataComDiferencaDias(1);
        if(DataUtils.verificarDiaSemana(agora, Calendar.SATURDAY)) {
            expectedDateEntrega = DataUtils.adicionarDias(agora,2);
        } else if(DataUtils.verificarDiaSemana(agora, Calendar.SUNDAY)) {
            expectedDateEntrega = DataUtils.adicionarDias(agora,1);
        }

        filmes.add(new Filme("The big short",10,5.0));

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação
        errorCollector.checkThat(locacao.getValor() > 0, CoreMatchers.is(true));
        errorCollector.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), expectedDateEntrega), CoreMatchers.is(true));
        errorCollector.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), expectedDateLocacaoHoje), CoreMatchers.is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void devePegarExceptionSemSaldoEstoqueElegante() {

        // cenario
//        LocacaoService locacaoService = new LocacaoService();
//        Usuario usuario = new Usuario("Fulano");
        filmes.add(new Filme("The big short",0,5.0));
        zeraEstoque(filmes);

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação é automática por causa da annotation


    }

    @Test
    public void devePegarExceptionSemSaldoEstoqueRobusta() {

        // cenario
//        LocacaoService locacaoService = new LocacaoService();
//        Usuario usuario = new Usuario("Fulano");
        filmes.add(new Filme("The big short",0,5.0));
        zeraEstoque(filmes);

        //execução
        try {
            Locacao locacao = locacaoService.alugarFilme(usuario,filmes);
            Assert.fail("Deveria lançar exceção");
        } catch (FilmeSemEstoqueException e) {
            Assert.assertThat(e.getMessage(), CoreMatchers.is("Filme sem estoque"));
        }

    }

    @Test
    public void devePegarExceptionUsuarioVazio() {

        // cenario
//        LocacaoService locacaoService = new LocacaoService();
        Filme filme = new Filme("The big short",10,5.0);

        //execução
        try {
            Locacao locacao = locacaoService.alugarFilme(null,filmes);
            Assert.fail("Deveria lançar exceção");
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), CoreMatchers.is("Usuário está nulo ou vazio"));
        }

    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void devePegarExceptionSemSaldoEstoqueRobustaExpectedException() {

        // cenario
//        LocacaoService locacaoService = new LocacaoService();
//        Usuario usuario = new Usuario("Fulano");
        filmes.add(new Filme("The big short",0,5.0));
        zeraEstoque(filmes);

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);
        //Assert.fail("Deveria lançar exceção");

    }

    @Test
    public void devePegarExceptionFilmeVazioExpectedException() {

        // cenario
//        LocacaoService locacaoService = new LocacaoService();
        Usuario usuario = new Usuario("Fulano");

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme está nulo ou vazio");

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,null);
        Assert.fail("Deveria lançar exceção");

    }


    @Test
    public void deveFalharDataLocacaoDiferenteHojeTest() {
        Date datataFuturo = DataUtils.obterDataComDiferencaDias(10);
        // cenario
//        LocacaoService locacaoService = new LocacaoService();
//        Usuario usuario = new Usuario("Fulano");
        Filme filme = new Filme("The big short",10,5.0);

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação
        Assert.assertFalse(DataUtils.isMesmaData(locacao.getDataLocacao(), datataFuturo));
    }

    @Test
    public void deveFalharSeDataEntregaNoPassadoTest() {
        Date datataFuturo = DataUtils.obterDataComDiferencaDias(10);
        // cenario
//        LocacaoService locacaoService = new LocacaoService();
//        Usuario usuario = new Usuario("Fulano");
        Filme filme = new Filme("The big short",10,5.0);

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação
        Assert.assertFalse(DataUtils.compararData(new Date(), locacao.getDataRetorno()) == -1);
    }

    private void zeraEstoque(List<Filme> filmes) {
        for(Filme filmeItem : filmes) {
            filmeItem.setEstoque(0);
        }
    }

}
