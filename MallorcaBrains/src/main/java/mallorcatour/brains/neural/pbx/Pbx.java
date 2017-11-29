package mallorcatour.brains.neural.pbx;

import mallorcatour.brains.HavingStats;
import mallorcatour.brains.neural.IPokerNeurals;
import mallorcatour.brains.stats.IPokerStats;
import mallorcatour.brains.stats.PokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class Pbx implements IPokerNeurals, HavingStats {

	private NeuralNetwork<?> preflop;
	private NeuralNetwork<?> flop;

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
		return new PokerStats(0.99, 0.99, 0.96, 0.95);
	}

	@Override
	public String getName() {
		return "PBX";
	}

}
