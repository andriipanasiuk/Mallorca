package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.situation.StreetEquity;

public interface PreflopEquilator {

    StreetEquity equityVsRandom(Card holeCard1, Card holeCard2);
}
