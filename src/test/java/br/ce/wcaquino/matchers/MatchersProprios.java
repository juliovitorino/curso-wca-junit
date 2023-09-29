package br.ce.wcaquino.matchers;

import java.util.Calendar;

public class MatchersProprios {
    public static DiaSemanaMatcher caiEm(Integer diaSemana) {
        return new DiaSemanaMatcher(diaSemana);
    }
    public static DiaSemanaMatcher caiNumaSegunda() {
        return new DiaSemanaMatcher(Calendar.MONDAY);
    }

    public static HojeMatcher ehHoje() {
        return new HojeMatcher();
    }

    public static AmanhaMatcher ehAmanha() {
        return new AmanhaMatcher();
    }

    public static HojeMatcher ehHojeComDiferencaDias(int dias) {
        return new HojeMatcher(dias);
    }
}
