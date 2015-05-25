/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;

/**
 *
 * @author Andrew
 */
public interface IExternalAdvisor {

    public final static IExternalAdvisor EMPTY = new IExternalAdvisor() {

        public Action getAction(IPlayerGameInfo gameInfo) {
            return Action.foldAction();
        }
    };

    public Action getAction(IPlayerGameInfo gameInfo);
}
