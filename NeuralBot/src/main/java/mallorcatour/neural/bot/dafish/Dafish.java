package mallorcatour.neural.bot.dafish;

import mallorcatour.neural.bot.BaseNeural;
import mallorcatour.neural.core.NeuralCreator;
import mallorcatour.stats.PokerStats;
import mallorcatour.stats.PokerStatsImpl;

public class Dafish extends BaseNeural {

	public Dafish() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralDafish());
		flop = NeuralCreator.createPerceptron(new FlopNeuralDafish());
		turn = NeuralCreator.createPerceptron(new TurnNeuralDafish());
		river = NeuralCreator.createPerceptron(new RiverNeuralDafish());
	}

	public PokerStats getStats() {
		return new PokerStatsImpl(0.83, 0.72, 0.33, 0.74);
	}

	@Override
	public String toString() {
		return "DaFish Neural";
	}

}
