package br.ce.wcaquino.matchers;

import br.ce.wcaquino.utils.DataUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaSemanaMatcher extends TypeSafeMatcher<Date> {

    private Integer diaSemana;
    private List<Integer> diasDaSemana;

    public DiaSemanaMatcher(Integer diaSemana) {
        this(diaSemana, null);
    }
    public DiaSemanaMatcher(List<Integer> diasDaSemana) {
        this(null, diasDaSemana);
    }
    public DiaSemanaMatcher(Integer diaSemana, List<Integer> diasDaSemana) {
        this.diaSemana = diaSemana;
        this.diasDaSemana = diasDaSemana;
    }
    @Override
    protected boolean matchesSafely(Date date) {
        boolean isDia = false;
        if(diasDaSemana == null) {
            return DataUtils.verificarDiaSemana(date,diaSemana);
        } else {
            for(Integer dia : diasDaSemana) {
                isDia = DataUtils.verificarDiaSemana(date,dia);
                if(isDia) break;
            }
        }
        return isDia;
    }

    @Override
    public void describeTo(Description description) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, diaSemana);
        String dataExtenso = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, new Locale("pt", "BR"));
        description.appendText(dataExtenso);

    }
}
