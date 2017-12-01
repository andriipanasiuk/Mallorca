package mallorcatour.neural.bot.france;

import mallorcatour.neural.bot.BaseNeural;
import mallorcatour.neural.core.NeuralCreator;
import mallorcatour.core.stats.PokerStats;
import mallorcatour.core.stats.PokerStatsImpl;

public class France extends BaseNeural {

	public France() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeural());
		flop = NeuralCreator.createPerceptron(new FlopNeural());
		turn = NeuralCreator.createPerceptron(new TurnNeural());
		river = NeuralCreator.createPerceptron(new RiverNeural());
	}

	public PokerStats getStats() {
		return new PokerStatsImpl(0.79, 0.53, 0.37, 0.24);
	}

	@Override
	public String toString() {
		return "France Neural";
	}
}
