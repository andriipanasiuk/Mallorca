package mallorcatour.bot.math;

import java.util.Map.Entry;

import mallorcatour.core.game.Action;
import mallorcatour.core.math.RandomVariable;

public class ActionUtils{

	public static RandomVariable maxEV(ActionDistribution actionDistribution) {
		double maxValue = -Double.MAX_VALUE;
		RandomVariable result = null;
		for (Entry<Action, RandomVariable> entry : actionDistribution.entrySet()) {
			RandomVariable item = entry.getValue();
			double ev = item.getEV();
			if (ev > maxValue) {
				result = item;
				maxValue = ev;
			}
		}
		if (result == null) {
			throw new RuntimeException();
		}
		return result;
	}
	
}