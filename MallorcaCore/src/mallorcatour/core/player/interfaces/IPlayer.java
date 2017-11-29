/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.player.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;

/**
 *
 * @author Andrew
 */
public interface IPlayer extends IGameObserver<IPlayerGameInfo>, IHoleCardsObserver, HasName {

    Action getAction();

}
