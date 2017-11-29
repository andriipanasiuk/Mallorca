package mallorcatour.bot.math;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.tools.Log;

import static mallorcatour.core.equilator.PokerEquilatorBrecher.LOGGING;

public class ProfitCalculatorTest {

    public static void test() {
        LOGGING = true;
        Spectrum spectrum = Spectrum.empty();
        spectrum.add(HoleCards.valueOf("AdJs"), 0.5);
        spectrum.add(HoleCards.valueOf("6dTd"), 0.2);
        spectrum.add(HoleCards.valueOf("7dKd"), 0.7);
        spectrum.add(HoleCards.valueOf("8dTd"), 0.4);
        double equity = ProfitCalculator.profitVsSpectrum(
                Card.valueOf("As"),
                Card.valueOf("Qs"),
                new Card[] { Card.valueOf("Jc"), Card.valueOf("Qc"), Card.valueOf("6c"), Card.valueOf("8c"),
                        Card.valueOf("9d") }, spectrum);
        Log.d("Result: " + equity);
    }
}