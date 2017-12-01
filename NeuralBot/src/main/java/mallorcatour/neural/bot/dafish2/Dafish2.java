package mallorcatour.neural.bot.dafish2;

import mallorcatour.neural.bot.BaseNeural;
import mallorcatour.neural.core.NeuralCreator;
import mallorcatour.core.stats.PokerStats;
import mallorcatour.core.stats.PokerStatsImpl;

public class Dafish2 extends BaseNeural {

	public Dafish2() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralDafish2());
		flop = NeuralCreator.createPerceptron(new FlopNeuralDafish2());
		turn = NeuralCreator.createPerceptron(new TurnNeuralDafish2());
		river = NeuralCreator.createPerceptron(new RiverNeuralDafish2());
	}

	public PokerStats getStats() {
		return new PokerStatsImpl(0.66, 0.53, 0.35, 0.62);
	}

	@Override
	public String toString() {
		return "DaFish2 Neural";
	}

}
