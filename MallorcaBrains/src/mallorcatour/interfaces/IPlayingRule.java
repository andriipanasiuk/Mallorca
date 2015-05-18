/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.IGameInfo;

/**
 *
 * @author Andrew
 */
public interface IPlayingRule {

    boolean isSatisfyCondition(IGameInfo gameInfo, HoleCards holeCards,
            String villainName);

    /**
     *@throws IllegalArgumentException if gameInfo does not satisfy condition
     */
    Action getAction(IGameInfo gameInfo, HoleCards holeCards, String villainName);
}
