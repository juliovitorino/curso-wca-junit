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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.matchers.MatchersProprios.podeSerNosDias;
import static br.ce.wcaquino.utils.DataUtils.adicionarDias;
import static br.ce.wcaquino.utils.DataUtils.compararData;
import static br.ce.wcaquino.utils.DataUtils.isMesmaData;
import static br.ce.wcaquino.utils.DataUtils.obterData;
import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;
import static br.ce.wcaquino.utils.DataUtils.verificarDiaSemana;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(PowerMockRunner.class)
@PrepareForTest(LocacaoService.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class LocacaoServiceTest {

    @Mock private LocacaoDAO locacaoDaoMock;
    @Mock private SPCService spcServiceMock;
    @Mock private EmailService emailServiceMock;
    @InjectMocks private LocacaoService locacaoService;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        // CRia os mocks que vão implementar os comportamentos aplicados nas suas respectivas interfaces
//        locacaoDaoMock = Mockito.mock(LocacaoDAO.class);
//        spcServiceMock = Mockito.mock(SPCService.class);
//        emailServiceMock = Mockito.mock(EmailService.class);

        MockitoAnnotations.initMocks(this);
        //locacaoService = new LocacaoService(locacaoDaoMock,spcServiceMock,emailServiceMock);
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
    public void devePermitirProrrogarlocacao() {
        // cenario
        Locacao locacao = LocacaoBuilder.builder().build();

        //ação
        locacaoService.prorrogarLocacao(locacao,3);

        // verificação
        ArgumentCaptor<Locacao> argumentCaptor = ArgumentCaptor.forClass(Locacao.class);
        Mockito.verify(locacaoDaoMock).salvar(argumentCaptor.capture());
        Locacao locacaoRetornada = argumentCaptor.getValue();

        errorCollector.checkThat(locacaoRetornada.getValor(), CoreMatchers.is(12.0));
        errorCollector.checkThat(locacaoRetornada.getDataLocacao(), MatchersProprios.ehHoje());
        errorCollector.checkThat(locacaoRetornada.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(3));
    }
    @Test
    public void deveChecarErroDeConsultaAoSPC() {
        // cenario
        Usuario usuario = UsuarioBuilder.builder().build();;
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Problemas com SPC, tente novamente");

        Mockito.when(spcServiceMock.possuiNegativacao(usuario)).thenThrow(new RuntimeException("Falha catastrófica") );


        // ação sob teste
        locacaoService.alugarFilme(usuario,filmes);


        // verificação é automática
    }
    @Test
    public void deveEnviarEmailParaLocacaoAtrasada() {

        //cenario
        Usuario usuario = UsuarioBuilder.builder().build();
        Usuario usuario2 = UsuarioBuilder.builder().comNome("Jonas Banana").build();
        Usuario usuario3 = UsuarioBuilder.builder().comNome("Clevekand").build();
        List<Locacao> locacoes = Arrays.asList(
                LocacaoBuilder.builder().atrasado().comUsuario(usuario).build(),
                LocacaoBuilder.builder().atrasado().comUsuario(usuario3).build(),
                LocacaoBuilder.builder().atrasado().comUsuario(usuario3).build(),
                LocacaoBuilder.builder().comUsuario(usuario2).build()
        );
        /*
         * Leia-se; Mockito, quando for chamado o método obterLocacoesPendentes() então retorne uma lista de locações
         * vide o método em LocacaoService.obterLocacoesPendentes
         *
         * Vamos criar a expectativa when() ... thenReturn()
         */
        Mockito.when(locacaoDaoMock.obterLocacoesPendentes()).thenReturn(locacoes);

        //acao
        locacaoService.notificarAtrasos();;

        //validação
        /*
         * Leia-se; Mockito, verifique REALMENTE quando for executado o método notificarAtraso com o usuário do cenário
         * Ou seja, usuários 1 e 3 executaram o método, enquanto usuário 2 NUNCA passou pelo notificar atraso. Fantastico!
         */
        Mockito.verify(emailServiceMock).notificarAtraso(usuario);
        Mockito.verify(emailServiceMock, Mockito.times(2)).notificarAtraso(usuario3);
        Mockito.verify(emailServiceMock, Mockito.never()).notificarAtraso(usuario2);
        Mockito.verifyNoMoreInteractions(emailServiceMock);

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
    public void deveDevolverFilmeNaSegundaAoAlugarNoSabado() throws Exception {

        // assume que a execução deste teste será realizada somente em um sábado. No futuro o powermock vai resolver isso
        // gerando um mock de Date()
        //
        // Assume.assumeTrue(verificarDiaSemana(new Date(), Calendar.SATURDAY));

        // cenario
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

        // Isso vai permitir que a unidade de teste execute em qualquer dia da semana, não somente so sábado
        // como era feito no ajuste de Assume. Resolve o problema de uma vez por todas
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(29,4,2017));

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

        //Assume.assumeTrue(verificarDiaSemana(new Date(),Calendar.SATURDAY));
        // cenario
        Date agora = new Date();
        Date expectedDateLocacaoHoje = new Date();
        Date expectedDateEntrega = obterDataComDiferencaDias(expectedDateLocacaoHoje,1);
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
        errorCollector.checkThat(locacao.getDataRetorno(), podeSerNosDias(Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)));
        errorCollector.checkThat(locacao.getDataLocacao(), ehHoje());
    }

    @Test
    public void deveAlugarNoSabadoDevolverNaSegunda() throws Exception {

        // cenario
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

        // Força uma data no sábado porque se o teste for tentar uma new Date dentro de locacaoService.alugarFilme()
        // e não for um sabado, vai falhar.
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(30,9,2023));

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação é automática por causa da annotation
        errorCollector.checkThat(locacao.getDataRetorno(), podeSerNosDias(Arrays.asList(Calendar.MONDAY)));
        errorCollector.checkThat(isMesmaData(locacao.getDataRetorno(),obterData(2,10,2023)), CoreMatchers.is(true));
    }

    @Test
    public void deveAlugarNoSabadoDevolverNaSegundaV2() throws Exception {

        // cenario
        Usuario usuario = UsuarioBuilder.builder().build();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.builder().build());

        // Força uma data no sábado porque se o teste for tentar uma new Date dentro de locacaoService.alugarFilme()
        // e não for um sabado, vai falhar.

        //Mockando o método estatico Calendar.getInstance
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH,30);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        calendar.set(Calendar.YEAR,2023);
        PowerMockito.mockStatic(Calendar.class);
        PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);

        //execução
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação é automática por causa da annotation
        errorCollector.checkThat(locacao.getDataRetorno(), podeSerNosDias(Arrays.asList(Calendar.MONDAY)));
        errorCollector.checkThat(isMesmaData(locacao.getDataRetorno(),obterData(2,10,2023)), CoreMatchers.is(true));
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
