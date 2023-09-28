package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {

    private LocacaoService locacaoService;

    @Parameterized.Parameter(value = 0)
    public List<Filme> filmes;

    @Parameterized.Parameter(value = 1)
    public Double valorLocacao;

    @Parameterized.Parameter(value = 2)
    public String cenario;

    private static Filme filme1 = new Filme("The big short",10,4.0);
    private static Filme filme2 = new Filme("Vanhelsing",10,4.0);
    private static Filme filme3 = new Filme("Transporter 3",10,4.0);
    private static Filme filme4 = new Filme("E o vento levou",10,4.0);
    private static Filme filme5 = new Filme("Titanic",10,4.0);
    private static Filme filme6 = new Filme("O Alto da compadecida",10,4.0);
    private static Filme filme7 = new Filme("Tenet",10,4.0);

    @Before
    public void setup() {
        locacaoService = new LocacaoService();
    }

    // conjunto de dados que será testado, o JUnit vai enviar uma linha por vez deste array
    @Parameterized.Parameters(name = "Teste {index} = {2}")
    public static Collection<Object[]> getParametros() {
        return Arrays.asList( new Object[][] {
                {Arrays.asList(filme1,filme2),8.0, " até 2 filmes - Sem Desconto"},
                {Arrays.asList(filme1,filme2, filme3),11.0, " 3 filmes - Desconto 25%"},
                {Arrays.asList(filme1,filme2, filme3, filme4),13.0, "4 filmes - Desconto 50%"},
                {Arrays.asList(filme1,filme2, filme3, filme4, filme5),14.0, "5 filmes - Desconto 75%"},
                {Arrays.asList(filme1,filme2, filme3, filme4, filme5, filme6),14.0, "6 filmes - Desconto 100%"},
                {Arrays.asList(filme1,filme2, filme3, filme4, filme5, filme6, filme7),18.0, "7 filmes - Sem desconto"}

        });
    }

    @Test
    public void deveAplicarDescontoConsiderandoTabela() {
        //cenario
        Usuario usuario = new Usuario("Albert Einstein");

        //ação
        Locacao locacao = locacaoService.alugarFilme(usuario,filmes);

        //validação
        Assert.assertEquals(valorLocacao, locacao.getValor());
    }

    @Test
    public void imprimirParametros() {
        System.out.println(valorLocacao);
    }
}
