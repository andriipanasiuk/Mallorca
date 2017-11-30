package mallorcatour.equilator;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.SpectrumEquilator;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.equilator.SpectrumEquilatorImpl;
import mallorcatour.tools.Log;

public class ProfitCalculatorTest {

    SpectrumEquilator equilator = new SpectrumEquilatorImpl();

    public void test() {
        Spectrum spectrum = Spectrum.empty();
        spectrum.add(HoleCards.valueOf("AdJs"), 0.5);
        spectrum.add(HoleCards.valueOf("6dTd"), 0.2);
        spectrum.add(HoleCards.valueOf("7dKd"), 0.7);
        spectrum.add(HoleCards.valueOf("8dTd"), 0.4);
        double equity = equilator.strengthVsSpectrum(
                Card.valueOf("As"),
                Card.valueOf("Qs"),
                new Card[] { Card.valueOf("Jc"), Card.valueOf("Qc"), Card.valueOf("6c"), Card.valueOf("8c"),
                        Card.valueOf("9d") }, spectrum);
        Log.d("Result: " + equity);
    }
}