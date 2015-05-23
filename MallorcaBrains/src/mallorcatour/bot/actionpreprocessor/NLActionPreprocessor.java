package mallorcatour.bot.actionpreprocessor;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
import mallorcatour.core.game.interfaces.IGameInfo;

/** 
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class NLActionPreprocessor implements IActionPreprocessor {

	@Override
    public Action preprocessAction(Action action, IGameInfo gameInfo) {
        double toCall = gameInfo.getHeroAmountToCall();
        double pot = gameInfo.getPotSize();
        double effectiveStack = gameInfo.getBankRollAtRisk();
        if (gameInfo.isVillainSitOut()) {
            return Action.createRaiseAction(toCall, pot, Double.MAX_VALUE);
        }
        if (action.isAllin()) {
        	action.setAmount(Double.MAX_VALUE);
            return action;
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
            if (toCall != 0) {
                if (!gameInfo.canHeroRaise()) {
                    return Action.callAction(toCall);
                }
                if (gameInfo.isPreFlop()) {
                    return Action.createRaiseAction(toCall, pot, effectiveStack, 1);
                } else {
                    Action resultAction = Action.createRaiseAction(toCall, pot, effectiveStack);
                    double amount = resultAction.getAmount();
                    amount = Math.round(amount / gameInfo.getBigBlindSize()) * gameInfo.getBigBlindSize();
                    resultAction.setAmount(amount);
                    return resultAction;
                }
            } else {
                if (gameInfo.isPreFlop()) {
                    return Action.createBetAction(pot, 1, effectiveStack);
                } else {
                    Action resultAction = Action.createBetAction(pot, 0.75, effectiveStack);
                    double amount = resultAction.getAmount();
                    amount = Math.round(amount / gameInfo.getBigBlindSize()) * gameInfo.getBigBlindSize();
                    resultAction.setAmount(amount);
                    return resultAction;
                }
            }
        } else {
            throw new RuntimeException();
        }
    }
}
