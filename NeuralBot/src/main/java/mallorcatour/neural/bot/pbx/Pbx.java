package mallorcatour.neural.bot.pbx;

import mallorcatour.neural.bot.IPokerNeurals;
import mallorcatour.stats.IPokerStats;
import mallorcatour.stats.PokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class Pbx implements IPokerNeurals {

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

	public IPokerStats getStats() {
		return new PokerStats(0.99, 0.99, 0.96, 0.95);
	}

	@Override
	public String getName() {
		return "PBX";
	}

}
