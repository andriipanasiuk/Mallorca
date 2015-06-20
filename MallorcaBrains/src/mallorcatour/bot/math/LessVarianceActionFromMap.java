/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import java.util.Map.Entry;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IGameInfo;

/**
 *
 * @author Andrew
 */
public class LessVarianceActionFromMap extends BaseAdviceCreatorFromMap {

	@Override
	public IAdvice create(ActionDistribution actionDistribution, IGameInfo gameInfo) {
		Entry<Action, RandomVariable> bestAction = getBestAction(actionDistribution, gameInfo.getBigBlindSize(), 0);
		return bestAction.getKey();
	}

	public static RandomVariable getOptimal(ActionDistribution actionDistribution, double bigBlind, double investment) {
		Entry<Action, RandomVariable> bestAction = getBestAction(actionDistribution, bigBlind, investment);
		return bestAction.getValue();
	}

	private static boolean condition(double profitability, double variance, double bigBlind) {
		return ((profitability > 0.25 && variance < 10 * bigBlind) || 
				(profitability > 0.5 && variance < 50 * bigBlind) || 
				(profitability > 0.75 && variance < 100 * bigBlind));
	}

	private static Entry<Action, RandomVariable> getBestAction(ActionDistribution actionDistribution, 
			double bigBlind, double investment) {
		double maxValue = -Double.MAX_VALUE;
		Entry<Action, RandomVariable> optimalAction = null;
		Entry<Action, RandomVariable> foldAction = null;
		for (Entry<Action, RandomVariable> entry : actionDistribution.entrySet()) {
			Action action = entry.getKey();
			double actionInvestment = investment;
			if (action.isFold()) {
				foldAction = entry;
			} else if (action.isPassive()) {
				actionInvestment += action.getAmountToCall();
			} else {
				actionInvestment += action.getAmountToCall();
				actionInvestment += action.getAmount();
			}
			RandomVariable item = entry.getValue();
			double ev = item.getEV();
			double profitability = actionInvestment == 0 ? Double.MAX_VALUE : ev / actionInvestment;
//			Log.f("Prftblt of " + action + " = " + profitability);
			double variance = item.getVariance();
			if (condition(profitability, variance, bigBlind) && ev > maxValue) {
				maxValue = ev;
				optimalAction = entry;
			}
		}
		if (optimalAction == null) {
			return foldAction;
		}
		return optimalAction;
	}
}
