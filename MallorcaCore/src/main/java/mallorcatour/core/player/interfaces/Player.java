package mallorcatour.core.player.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.interfaces.IPlayerGameObserver;

public interface Player extends IPlayerGameObserver, IHoleCardsObserver {

    Action getAction();

    String getName();

}
