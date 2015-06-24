package mallorcatour.bot.math;

import java.util.HashMap;

import mallorcatour.core.game.Action;
import mallorcatour.tools.FileUtils;

public class ActionDistribution extends HashMap<Action, RandomVariable> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3270697821017287382L;

	public String toSmartString(double bb, double heroES) {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		for (java.util.Map.Entry<Action, RandomVariable> entry : entrySet()) {
			Action action = entry.getKey();
			builder.append(action);
			builder.append(" = ");
			RandomVariable profit = entry.getValue();
			builder.append(profit.printProfitability(action, heroES, bb));
			builder.append(FileUtils.LINE_SEPARATOR);
		}
		builder.append("}");
		return builder.toString();
	}
}