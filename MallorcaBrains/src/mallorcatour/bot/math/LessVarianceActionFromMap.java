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
		Entry<Action, RandomVariable> bestAction = getBestAction(actionDistribution, gameInfo.getBigBlindSize());
		return bestAction.getKey();
	}

	public static RandomVariable getOptimal(ActionDistribution actionDistribution, double bigBlind) {
		Entry<Action, RandomVariable> bestAction = getBestAction(actionDistribution, bigBlind);
		return bestAction.getValue();
	}

	private static Entry<Action, RandomVariable> getBestAction(ActionDistribution actionDistribution, double bigBlind) {
		double maxValue = -Double.MAX_VALUE;
		double diff = -2.5 * bigBlind;
		Entry<Action, RandomVariable> optimalAction = null;
		Entry<Action, RandomVariable> foldAction = null;
		for (Entry<Action, RandomVariable> entry : actionDistribution.entrySet()) {
			Action action = entry.getKey();
			if (action.isFold()) {
				foldAction = entry;
			}
			RandomVariable item = entry.getValue();
			double ev = item.getEV();
			double variance = item.getVariance();
			if (ev - variance > diff && ev > maxValue) {
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
