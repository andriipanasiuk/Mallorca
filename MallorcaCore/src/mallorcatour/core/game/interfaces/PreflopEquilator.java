package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.state.StreetEquity;
import mallorcatour.core.spectrum.Spectrum;

public interface PreflopEquilator {

    StreetEquity equityVsRandom(Card holeCard1, Card holeCard2);

    double strengthVsRandom(Card heroCard1, Card heroCard2);

    double strengthVsSpectrum(Card heroCard1, Card heroCard2, Spectrum villainSpectrum);
}
