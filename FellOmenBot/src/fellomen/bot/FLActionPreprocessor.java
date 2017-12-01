package fellomen.bot;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.game.interfaces.IActionPreprocessor;

/** 
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class FLActionPreprocessor implements IActionPreprocessor {

    public Action preprocessAction(Action action, GameContext gameInfo, double toCall) {
        if (action.isFold()) {
            if (toCall == 0) {
                return Action.checkAction();
            } else {
                return Action.fold();
            }
        } else if (action.isPassive()) {
            if (toCall != 0) {
                return Action.callAction(toCall);
            } else {
                return Action.checkAction();
            }
        } else if (action.isAggressive()) {
            //TODO check if hero can raise
            if (toCall != 0 /*&& !flGameContext.canHeroRaise()*/) {
                return Action.callAction(toCall);
            }
            if (gameInfo.isPreFlop() || gameInfo.isFlop()) {
                return Action.createRaiseAction(gameInfo.getBigBlindSize(), -1);
            } else {
                return Action.createRaiseAction(2 * gameInfo.getBigBlindSize(), -1);
            }
        } else {
            throw new RuntimeException();
        }
    }
}
