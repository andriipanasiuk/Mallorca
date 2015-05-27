package mallorcatour.bot.actionpreprocessor;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;

/**
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class NLActionPreprocessor implements IActionPreprocessor {

	@Override
	public Action preprocessAction(Action action, IPlayerGameInfo gameInfo) {
		double toCall = gameInfo.getAmountToCall();
		double pot = gameInfo.getPotSize();
		double effectiveStack = gameInfo.getBankRollAtRisk();
		if (action.isAllin()) {
			action.setAmount(effectiveStack);
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
			if (toCall != 0 && !gameInfo.canHeroRaise()) {
				return Action.callAction(toCall);
			}
			double percent;
			if (toCall != 0) {
				percent = 1;
			} else {
				percent = 0.75;
			}
			if (gameInfo.isPreFlop()) {
				return Action.createRaiseAction(toCall, pot, effectiveStack, 1);
			} else {
				Action resultAction = Action.createRaiseAction(toCall, pot, effectiveStack, percent);
				double amount = resultAction.getAmount();
				if (amount != effectiveStack) {
					amount = Math.round(amount / gameInfo.getBigBlindSize()) * gameInfo.getBigBlindSize();
					resultAction.setAmount(amount);
				}
				return resultAction;
			}
		} else {
			throw new RuntimeException();
		}
	}
}
