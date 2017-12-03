package mallorcatour.core.player.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.interfaces.IGameObserver;

public interface Player extends IGameObserver, IHoleCardsObserver {

    Action getAction();

    String getName();

}
