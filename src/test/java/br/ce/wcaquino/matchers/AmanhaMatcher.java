package br.ce.wcaquino.matchers;

import br.ce.wcaquino.utils.DataUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Date;

public class AmanhaMatcher extends TypeSafeMatcher<Date> {
    @Override
    protected boolean matchesSafely(Date date) {
        Date hoje = new Date();
        Date amanha = DataUtils.adicionarDias(hoje,+1);
        return DataUtils.isMesmaData(date, amanha);
    }

    @Override
    public void describeTo(Description description) {

    }
}
