package mallorcatour.neural.bot.gusxensen;

import mallorcatour.neural.bot.BaseNeural;
import mallorcatour.neural.bot.IPokerNeurals;
import mallorcatour.stats.PokerStats;
import mallorcatour.stats.PokerStatsImpl;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class GusXensen extends BaseNeural {

	public GusXensen() {
		super();
	}

	public PokerStats getStats() {
		return new PokerStatsImpl(0.79, 0.52, 0.37, 0.56);
	}

	@Override
	public String toString() {
		return "GusXensen Neural";
	}
}
