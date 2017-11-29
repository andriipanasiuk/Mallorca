package mallorcatour.neural.bot.germany;

import mallorcatour.neural.bot.IPokerNeurals;
import mallorcatour.stats.IPokerStats;
import mallorcatour.stats.PokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class Germany implements IPokerNeurals {

	private NeuralNetwork<?> preflop, flop;

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

	@Override
	public NeuralNetwork<?> getFlop() {
		return flop;
	}

	@Override
	public NeuralNetwork<?> getPreflop() {
		return preflop;
	}

	public IPokerStats getStats() {
		return new PokerStats(0.7, 0.28, 0.65, 0.22);
	}

	@Override
	public String getName() {
		return "Germany";
	}
}
