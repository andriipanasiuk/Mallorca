/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IPlayerGameObserver;

/**
 *
 * @author Andrew
 */
public interface IPlayer extends IPlayerGameObserver {

    Action getAction();

    String getName();

}
