/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.humanadvisor;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IGameInfo;

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
