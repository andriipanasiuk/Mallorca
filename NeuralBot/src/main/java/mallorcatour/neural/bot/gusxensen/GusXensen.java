package mallorcatour.neural.bot.gusxensen;

import mallorcatour.neural.bot.IPokerNeurals;
import mallorcatour.stats.IPokerStats;
import mallorcatour.stats.PokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class GusXensen implements IPokerNeurals {

	private NeuralNetwork<?> preflop, flop, turn, river;

	public GusXensen() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralGX());
		flop = NeuralCreator.createPerceptron(new FlopNeuralGX());
		turn = NeuralCreator.createPerceptron(new TurnNeuralGX());
		river = NeuralCreator.createPerceptron(new RiverNeuralGX());
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
		return new PokerStats(0.79, 0.52, 0.37, 0.56);
	}

	@Override
	public String getName() {
		return "GusXensen";
	}
}
