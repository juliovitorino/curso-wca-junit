package br.ce.wcaquino.matchers;

import br.ce.wcaquino.utils.DataUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Date;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;
import static br.ce.wcaquino.utils.DataUtils.isMesmaData;

public class HojeMatcher extends TypeSafeMatcher<Date> {

    private int dias;

    public HojeMatcher() {
        this(0);
    }
    public HojeMatcher(int dias) {
        this.dias = dias;
    }
    @Override
    protected boolean matchesSafely(Date dateCheck) {
        Date dataComDias = new Date();
        if(dias > 0) {
            dataComDias = adicionarDias(dataComDias, dias);
        }
        return isMesmaData(dateCheck, dataComDias);
    }

    @Override
    public void describeTo(Description description) {

    }
}
