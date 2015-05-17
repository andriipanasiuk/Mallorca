package mallorcatour.core.bot;

import mallorcatour.game.core.Action;
import mallorcatour.interfaces.IActionPreprocessor;

/** 
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class FLActionPreprocessor implements IActionPreprocessor {

    public Action preprocessAction(Action action, IGameInfo gameInfo,
            String villainName) {
        double toCall = gameInfo.getHeroAmountToCall();
        if (gameInfo.getBankRoll(villainName) == IGameInfo.SITTING_OUT) {
            return Action.createRaiseAction(gameInfo.getBigBlindSize(), -1);
        }
        if (action.isFold()) {
            if (toCall == 0) {
                return Action.checkAction();
            } else {
                return Action.foldAction();
            }
        } else if (action.isPassive()) {
            if (toCall != 0) {
                return Action.callAction(toCall);
            } else {
                return Action.checkAction();
            }
        } else if (action.isAggressive()) {
            if (toCall != 0 && !gameInfo.canHeroRaise()) {
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
