/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IGameObserver;

/**
 *
 * @author Andrew
 */
public interface IPlayer extends IGameObserver {

    Action getAction();

}
