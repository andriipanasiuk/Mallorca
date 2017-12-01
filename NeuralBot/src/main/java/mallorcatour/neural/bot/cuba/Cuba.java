package mallorcatour.neural.bot.cuba;

import mallorcatour.neural.bot.BaseNeural;
import mallorcatour.neural.core.NeuralCreator;
import mallorcatour.core.stats.PokerStats;
import mallorcatour.core.stats.PokerStatsImpl;

public class Cuba extends BaseNeural {

	public Cuba() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralInfo());
		flop = NeuralCreator.createPerceptron(new FlopNeuralC());
		turn = NeuralCreator.createPerceptron(new TurnNeuralInfo());
		river = NeuralCreator.createPerceptron(new RiverNeuralInfo());
	}

	public PokerStats getStats() {
		return new PokerStatsImpl(0.91, 0.44, 0.9, 0.28);
	}

	@Override
	public String toString() {
		return "Cuba Neural";
	}

}
