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
		Entry<Action, RandomVariable> bestAction = getBestAction(actionDistribution, gameInfo, 0, true);
		return bestAction.getKey();
	}

	public static RandomVariable getOptimal(ActionDistribution actionDistribution, IGameInfo gameInfo, double investment) {
		Entry<Action, RandomVariable> bestAction = getBestAction(actionDistribution, gameInfo, investment, false);
		return bestAction.getValue();
	}

	public static int PROFITABILITY_CORRECTION = 0;
	public static int VARIANCE_CORRECTION = 0;

	private static boolean profitabilityIsGood(ActionProfit actionProfit, double bigBlind) {
		if (actionProfit == null) {
			return false;
		}
		double profitability = actionProfit.investment == 0 ? Double.MAX_VALUE : actionProfit.ev
				/ actionProfit.investment;
		double variance = actionProfit.variance;
		profitability += PROFITABILITY_CORRECTION * 0.01;
		variance += VARIANCE_CORRECTION * bigBlind;
		return (profitability > 0.2 && variance < 5 * bigBlind) || 
				(profitability > 0.25 && variance < 10 * bigBlind) || 
				(profitability > 0.4 && variance < 20 * bigBlind) || 
				(profitability > 0.5 && variance < 40 * bigBlind) || 
				(profitability > 0.55 && variance < 50 * bigBlind) || 
				(profitability > 0.75 && variance < 100 * bigBlind);
	}

	private static class ActionProfit {
		double ev, variance, investment;
	}

	private static Entry<Action, RandomVariable> getBestAction(ActionDistribution actionDistribution, 
			IGameInfo gameInfo, double investment, boolean fromTop) {
		double bigBlind = gameInfo.getBigBlindSize();
		Entry<Action, RandomVariable> foldAction = null, passiveAction = null, raiseAction = null;
		ActionProfit passiveProfit = null, raiseProfit = null;
		for (Entry<Action, RandomVariable> entry : actionDistribution.entrySet()) {
			Action action = entry.getKey();
			double actionInvestment = investment;
			ActionProfit actionProfit = new ActionProfit();
			if (action.isFold()) {
				foldAction = entry;
			} else if (action.isPassive()) {
				passiveAction = entry;
				passiveProfit = actionProfit;
				actionInvestment += action.getAmountToCall();
			} else {
				raiseProfit = actionProfit;
				raiseAction = entry;
				actionInvestment += action.getAmountToCall();
				actionInvestment += action.getAmount();
			}
			actionProfit.investment = actionInvestment;
			RandomVariable item = entry.getValue();
			double variance = item.getVariance();
			actionProfit.variance = variance;

			double ev = item.getEV();
			actionProfit.ev = ev;
		}
		if (gameInfo.isPreFlop()  & fromTop) {
			passiveProfit.ev += 30 * bigBlind;
//			raiseProfit.ev += 30 * bigBlind;
		}
		if (!profitabilityIsGood(raiseProfit, bigBlind) && !profitabilityIsGood(passiveProfit, bigBlind)) {
			return foldAction;
		}
		if (passiveProfit == null) {
			throw new RuntimeException("Passive action must be");
		}
		if (profitabilityIsGood(raiseProfit, bigBlind)) {
			if (profitabilityIsGood(passiveProfit, bigBlind)) {
				if (raiseProfit.ev > passiveProfit.ev) {
					return raiseAction;
				} else {
					return passiveAction;
				}
			} else {
				return raiseAction;
			}
		} else {
			return passiveAction;
		}
	}
}
