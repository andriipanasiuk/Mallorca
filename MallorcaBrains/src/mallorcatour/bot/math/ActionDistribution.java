package mallorcatour.bot.math;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.core.game.Action;

public class ActionDistribution extends HashMap<Action, RandomVariable> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3270697821017287382L;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		for (Map.Entry<Action, RandomVariable> entry : entrySet()) {
			builder.append(entry.getKey());
			builder.append(" = ");
			builder.append(entry.getValue());
			builder.append(" ");
		}
		builder.append("}");
		return builder.toString();
	}
}