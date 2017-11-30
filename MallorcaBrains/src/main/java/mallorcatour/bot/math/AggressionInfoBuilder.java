package mallorcatour.bot.math;

import mallorcatour.core.game.interfaces.IAggressionInfo;

public class AggressionInfoBuilder {
	public static AggressionInfoBuffer build(IAggressionInfo info) {
		return new AggressionInfoBuffer(info);
	}

}