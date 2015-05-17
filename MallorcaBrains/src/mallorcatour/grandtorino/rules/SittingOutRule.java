/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.rules;

import mallorcatour.core.bot.IGameInfo;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.HoleCards;
import mallorcatour.interfaces.IPlayingRule;

/**
 *
 * @author Andrew
 */
public class SittingOutRule implements IPlayingRule {

    public boolean isSatisfyCondition(IGameInfo gameInfo, HoleCards holeCards,
            String villainName) {
        return gameInfo.getBankRoll(villainName) == IGameInfo.SITTING_OUT;
    }

    public Action getAction(IGameInfo gameInfo, HoleCards holeCards, String villainName) {
        if (!isSatisfyCondition(gameInfo, holeCards, villainName)) {
            throw new IllegalArgumentException();
        }
        double percent = 0.5;
        Action action = Action.createRaiseAction(percent
                * (gameInfo.getPotSize() + gameInfo.getHeroAmountToCall()), percent);
        return action;
    }
}
