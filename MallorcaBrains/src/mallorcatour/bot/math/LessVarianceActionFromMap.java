/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import java.util.Map.Entry;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.tools.Log;

/**
 *
 * @author Andrew
 */
public class LessVarianceActionFromMap extends BaseAdviceCreatorFromMap {

	@Override
	public IAdvice create(ActionDistribution actionDistribution, IGameInfo gameInfo) {
		double maxValue = -Double.MAX_VALUE;
		double diff = -2 * gameInfo.getBigBlindSize();
		Action action = null;
		for (Entry<Action, RandomVariable> entry : actionDistribution.entrySet()) {
			RandomVariable item = entry.getValue();
			double ev = item.getEV();
			double variance = item.getVariance();
			if (ev - variance > diff && ev > maxValue) {
				maxValue = ev;
				action = entry.getKey();
			}
		}
		if (action == null) {
			Log.d("!!!!!ALERT!!!! Choose fold action by default");
			return Action.fold();
			// throw new RuntimeException("No one variant for choose");
		}
		return action;
	}
}
