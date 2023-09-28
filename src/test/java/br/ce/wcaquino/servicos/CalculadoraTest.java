package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exception.DivisaoPorZeroException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CalculadoraTest {
    private Calculadora calc;

    @Before
    public void setup() {
        calc = new Calculadora();
    }

    @Test
    public void deveSomarDoisNumeros() {
        // cenario
        int a = 5;
        int b = 10;
        // execução
        int resultado = calc.somar(a,b);

        // validação
        Assert.assertEquals(15, resultado);
    }

    @Test
    public void deveSubtrairDoisNumeros() {
        //cenario
        int a = 10;
        int b = 5;

        //execução
        int resultado = calc.subtrair(a,b);

        //validação
        Assert.assertEquals(5,resultado);
    }

    @Test
    public void deveMultiuplicarDoisNumero() {
        int a = 5;
        int b = 10;

        int resultado = calc.multiplicar(a,b);

        Assert.assertEquals(50, resultado);
    }

    @Test
    public void deveDividirDoisNumeros() {
        int a = 6;
        int b = 3;

        int resultado = calc.dividir(a,b);

        Assert.assertEquals(2,resultado);

    }

    @Test(expected = DivisaoPorZeroException.class)
    public void naoPodeDividirPorZero() {
        int a = 10;
        int b = 0;

        int resultado = calc.dividir(a,b);
        Assert.fail("Deveria capturar exception");
    }


}
