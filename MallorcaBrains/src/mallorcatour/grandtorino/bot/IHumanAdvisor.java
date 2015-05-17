/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.bot;

import mallorcatour.core.bot.IGameInfo;
import mallorcatour.game.core.Action;

/**
 *
 * @author Andrew
 */
public interface IHumanAdvisor {

    public final static IHumanAdvisor EMPTY = new IHumanAdvisor() {

        public Action getHumanAction(IGameInfo gameInfo) {
            return Action.foldAction();
        }
    };

    public Action getHumanAction(IGameInfo gameInfo);
}
