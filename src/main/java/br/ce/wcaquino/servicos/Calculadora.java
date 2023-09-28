package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exception.DivisaoPorZeroException;

public class Calculadora {
    public int somar(int a, int b) {
        return a + b;
    }

    public int subtrair(int a, int b) {
        return a - b;
    }

    public int multiplicar(int a, int b) {
        return a * b;
    }

    public int dividir(int a, int b) {
        if( b == 0) {
            throw new DivisaoPorZeroException();
        }
        return a / b;
    }
}
