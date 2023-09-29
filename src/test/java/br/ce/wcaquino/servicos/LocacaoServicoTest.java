package br.ce.wcaquino.servicos;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.dao.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exception.FilmeSemEstoqueException;
import br.ce.wcaquino.exception.LocadoraException;
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
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.matchers.MatchersProprios.ehAmanha;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
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
    private LocacaoDAO locacaoDaoMock;
    private SPCService spcServiceMock;
    private EmailService emailServiceMock;
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        // CRia os mocks que vão implementar os comportamentos aplicados nas suas respectivas interfaces
        locacaoDaoMock = Mockito.mock(LocacaoDAO.class);
        spcServiceMock = Mockito.mock(SPCService.class);
        emailServiceMock = Mockito.mock(EmailService.class);

        locacaoService = new LocacaoService(locacaoDaoMock,spcServiceMock,emailServiceMock);
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
    public void deveEnviarEmailParaLocacaoAtrasada() {

        //cenario
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Locacao> locacoes = Arrays.asList(
                LocacaoBuilder.builder()
                        .comUsuario(usuario)
                        .comDataDeRetorno(DataUtils.obterDataComDiferencaDias(-2))
                        .build()
        );
        /*
         * Leia-se; Mockito, quando for chamado o método obterLocacoesPendentes() então retorne uma lista de locações
         * vide o método em LocacaoService.obterLocacoesPendentes
         */
        Mockito.when(locacaoDaoMock.obterLocacoesPendentes()).thenReturn(locacoes);

        //acao
        locacaoService.notificarAtrasos();;

        //validação
        /*
         * Leia-se; Mockito, verifique quando for executado o método notificarAtraso com o usuário do cenário
         */
        Mockito.verify(emailServiceMock).notificarAtraso(usuario);

    }
    @Test
    public void naoDeveAlugarFilmeParaUsuarioNegativadoSPC() {
        //cenario
        Usuario usuario = UsuarioBuilder.builder().build();
        Usuario usuario2 = UsuarioBuilder.builder().comNome("Usuario 2").build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

        /*
         * Ensina ao mockito o comportamento quando chamar o método da interface possuiNegativaçao
         *
         * Leia-se:
         * Mockito, quando invocar o método possuiNegativacao passando o usuario, então retorne true
         */
        Mockito.when(spcServiceMock.possuiNegativacao(usuario)).thenReturn(true);


        //ação
        try {
            locacaoService.alugarFilme(usuario, filmes);
            Assert.fail("Falso positivo. Não deveria chegar nesse ponto");
        } catch(LocadoraException e) {
            Assert.assertThat(e.getMessage(), CoreMatchers.is("Usuário negativado no SPC"));
        }

        //validação
        /*
         * Leia-se: Mockito, faça uma verificação se o serviço do SPCService invocou o método possui negativação para
         * o usuário do cenário
         */
        Mockito.verify(spcServiceMock).possuiNegativacao(usuario);
    }
    @Test
    public void deveDevolverFilmeNaSegundaAoAlugarNoSabado() {

        Assume.assumeTrue(verificarDiaSemana(new Date(), Calendar.SATURDAY));
        // cenario
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

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

        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

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
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().zerarEstoque().build());

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação é automática por causa da annotation


    }

    @Test
    public void devePegarExceptionSemSaldoEstoqueRobusta() {

        // cenario
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().zerarEstoque().build());;

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
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

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
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().zerarEstoque().build());

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
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação
        assertFalse(isMesmaData(locacao.getDataLocacao(), datataFuturo));
    }

    @Test
    public void deveFalharSeDataEntregaNoPassadoTest() {
        Date datataFuturo = obterDataComDiferencaDias(10);
        // cenario
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação
        assertFalse(compararData(new Date(), locacao.getDataRetorno()) == -1);
    }


}
