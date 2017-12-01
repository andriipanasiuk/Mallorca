package mallorcatour.neural.bot.checkburn;

import mallorcatour.neural.bot.BaseNeural;
import mallorcatour.core.stats.PokerStats;
import mallorcatour.core.stats.PokerStatsImpl;
import mallorcatour.neural.core.NeuralCreator;

public class CheckBurn extends BaseNeural {

	public CheckBurn() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralCCB());
		flop = NeuralCreator.createPerceptron(new FlopNeuralCCB());
		turn = NeuralCreator.createPerceptron(new TurnNeuralCCB());
		river = NeuralCreator.createPerceptron(new RiverNeuralCCB());
	}

	public PokerStats getStats() {
		return new PokerStatsImpl(0.5, 0.1, 0.29, 0.67);
	}

	@Override
	public String toString() {
		return "CheckCheckBurn Neural";
	}

}
