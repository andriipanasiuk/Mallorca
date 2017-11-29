package mallorcatour.neural.bot.cuba;

import mallorcatour.neural.bot.IPokerNeurals;
import mallorcatour.stats.IPokerStats;
import mallorcatour.stats.PokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class Cuba implements IPokerNeurals {

	private NeuralNetwork<?> preflop, flop, turn, river;

	public Cuba() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralInfo());
		flop = NeuralCreator.createPerceptron(new FlopNeuralC());
		turn = NeuralCreator.createPerceptron(new TurnNeuralInfo());
		river = NeuralCreator.createPerceptron(new RiverNeuralInfo());
	}

	@Override
	public NeuralNetwork<?> getRiver() {
		return river;
	}

	@Override
	public NeuralNetwork<?> getTurn() {
		return turn;
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
		return new PokerStats(0.91, 0.44, 0.9, 0.28);
	}

	@Override
	public String getName() {
		return "Cuba";
	}

}
