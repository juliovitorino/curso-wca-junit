package br.ce.wcaquino.servicos;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.dao.LocacaoDAO;
import br.ce.wcaquino.dao.LocacaoFakeDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exception.FilmeSemEstoqueException;
import br.ce.wcaquino.exception.LocadoraException;
import br.ce.wcaquino.matchers.MatchersProprios;
import org.junit.After;
import org.junit.AfterClass;
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

import static br.ce.wcaquino.matchers.MatchersProprios.ehAmanha;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHojeComDiferencaDias;
import static br.ce.wcaquino.utils.DataUtils.adicionarDias;
import static br.ce.wcaquino.utils.DataUtils.compararData;
import static br.ce.wcaquino.utils.DataUtils.isMesmaData;
import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;
import static br.ce.wcaquino.utils.DataUtils.verificarDiaSemana;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


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
        locacaoService.setLocacaoDAO(new LocacaoFakeDAO()); //  Injeta uma instância de dao fake
        usuario = UsuarioBuilder.builder().build();
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

        Assume.assumeTrue(verificarDiaSemana(new Date(), Calendar.SATURDAY));
        // cenario
        filmes.add(FilmeBuilder.builder().build());

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

        //validação
        boolean ehSegunda = verificarDiaSemana(locacao.getDataRetorno(),Calendar.MONDAY);
        assertThat(locacao.getDataRetorno(), MatchersProprios.caiEm(Calendar.MONDAY));
        assertThat(locacao.getDataRetorno(), MatchersProprios.caiNumaSegunda());
        assertTrue(ehSegunda);

    }

    @Test
    public void alugarFilmeComSucesso() {

        Assume.assumeTrue(verificarDiaSemana(new Date(),Calendar.SATURDAY));
        // cenario
        Date agora = new Date();
        Date expectedDateLocacaoHoje = new Date();
        Date expectedDateEntrega = obterDataComDiferencaDias(1);
        if(verificarDiaSemana(agora, Calendar.SATURDAY)) {
            expectedDateEntrega = adicionarDias(agora,2);
        } else if(verificarDiaSemana(agora, Calendar.SUNDAY)) {
            expectedDateEntrega = adicionarDias(agora,1);
        }

        filmes.add(FilmeBuilder.builder().build());

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação
        errorCollector.checkThat(locacao.getValor() > 0, is(true));
        errorCollector.checkThat(isMesmaData(locacao.getDataRetorno(), expectedDateEntrega), is(true));
        errorCollector.checkThat(isMesmaData(locacao.getDataLocacao(), expectedDateLocacaoHoje), is(true));

        // com matcher próprio
        errorCollector.checkThat(locacao.getDataRetorno(), ehAmanha());
        errorCollector.checkThat(locacao.getDataLocacao(), ehHoje());
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void devePegarExceptionSemSaldoEstoqueElegante() {

        // cenario
        filmes.add(FilmeBuilder.builder().zerarEstoque().build());

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação é automática por causa da annotation


    }

    @Test
    public void devePegarExceptionSemSaldoEstoqueRobusta() {

        // cenario
        //filmes.add(new Filme("The big short",0,5.0));
        filmes.add(FilmeBuilder.builder().zerarEstoque().build());;

        //execução
        try {
            Locacao locacao = locacaoService.alugarFilme(usuario,filmes);
            fail("Deveria lançar exceção");
        } catch (FilmeSemEstoqueException e) {
            assertThat(e.getMessage(), is("Filme sem estoque"));
        }

    }

    @Test
    public void devePegarExceptionUsuarioVazio() {

        // cenario
        Filme filme = FilmeBuilder.builder().build();

        //execução
        try {
            Locacao locacao = locacaoService.alugarFilme(null,filmes);
            fail("Deveria lançar exceção");
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuário está nulo ou vazio"));
        }

    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void devePegarExceptionSemSaldoEstoqueRobustaExpectedException() {

        // cenario
//        Usuario usuario = new Usuario("Fulano");
        filmes.add(FilmeBuilder.builder().zerarEstoque().build());

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);
        //Assert.fail("Deveria lançar exceção");

    }

    @Test
    public void devePegarExceptionFilmeVazioExpectedException() {

        // cenario
        Usuario usuario = UsuarioBuilder.builder().build();

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme está nulo ou vazio");

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,null);
        fail("Deveria lançar exceção");

    }


    @Test
    public void deveFalharDataLocacaoDiferenteHojeTest() {
        Date datataFuturo = obterDataComDiferencaDias(10);
        // cenario
        Filme filme = FilmeBuilder.builder().build();

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação
        assertFalse(isMesmaData(locacao.getDataLocacao(), datataFuturo));
    }

    @Test
    public void deveFalharSeDataEntregaNoPassadoTest() {
        Date datataFuturo = obterDataComDiferencaDias(10);
        // cenario
        Filme filme = FilmeBuilder.builder().build();

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação
        assertFalse(compararData(new Date(), locacao.getDataRetorno()) == -1);
    }


}
