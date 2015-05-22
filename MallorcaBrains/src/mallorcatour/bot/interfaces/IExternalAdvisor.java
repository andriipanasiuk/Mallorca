/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IGameInfo;

/**
 *
 * @author Andrew
 */
public interface IExternalAdvisor {

    public final static IExternalAdvisor EMPTY = new IExternalAdvisor() {

        public Action getAction(IGameInfo gameInfo) {
            return Action.foldAction();
        }
    };

    public Action getAction(IGameInfo gameInfo);
}
