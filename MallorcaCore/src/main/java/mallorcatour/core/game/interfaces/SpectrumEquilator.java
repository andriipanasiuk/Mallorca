package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Card;
import mallorcatour.core.spectrum.Spectrum;

public interface SpectrumEquilator {

    double strengthVsSpectrum(Card heroCard1, Card heroCard2, Card[] boardCards, Spectrum spectrum);
}
