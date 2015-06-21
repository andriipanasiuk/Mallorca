package mallorcatour.brains.neural.germany;

import mallorcatour.brains.HavingStats;
import mallorcatour.brains.neural.IPokerNeurals;
import mallorcatour.brains.stats.PokerStats;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class Germany implements IPokerNeurals, HavingStats {

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

	@Override
	public IPokerStats getStats() {
		return new PokerStats(0.7, 0.32, 0.65, 0.22);
	}

	@Override
	public String getName() {
		return "Germany";
	}
}
