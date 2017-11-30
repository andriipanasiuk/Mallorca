package mallorcatour.neural.bot.pbx;

import org.neuroph.core.NeuralNetwork;

import mallorcatour.neural.bot.BaseNeural;
import mallorcatour.neural.core.NeuralCreator;
import mallorcatour.stats.PokerStats;
import mallorcatour.stats.PokerStatsImpl;

public class Pbx extends BaseNeural {

	public Pbx() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralPBX());
		flop = NeuralCreator.createPerceptron(new FlopNeuralPBX());
	}

	@Override
	public NeuralNetwork<?> getRiver() {
		return flop;
	}

	@Override
	public NeuralNetwork<?> getTurn() {
		return flop;
	}

	public PokerStats getStats() {
		return new PokerStatsImpl(0.99, 0.99, 0.96, 0.95);
	}

	@Override
	public String toString() {
		return "PBX Neural";
	}

}
