package mallorcatour.neural.bot.germany;

import org.neuroph.core.NeuralNetwork;

import mallorcatour.neural.bot.BaseNeural;
import mallorcatour.neural.core.NeuralCreator;
import mallorcatour.stats.PokerStats;
import mallorcatour.stats.PokerStatsImpl;

public class Germany extends BaseNeural {

	public Germany() {
		preflop = NeuralCreator.createPerceptron(new Preflop());
		flop = NeuralCreator.createPerceptron(new FlopNeuralG());
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
		return new PokerStatsImpl(0.7, 0.28, 0.65, 0.22);
	}

	@Override
	public String toString() {
		return "Germany Neural";
	}
}
