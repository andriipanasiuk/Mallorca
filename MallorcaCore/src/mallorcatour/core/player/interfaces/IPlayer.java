package mallorcatour.core.player.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;

public interface IPlayer extends IGameObserver<IPlayerGameInfo>, IHoleCardsObserver {

    Action getAction();

    String getName();

}
