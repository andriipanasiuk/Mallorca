package mallorcatour.brains.neural.gusxensen;

import mallorcatour.brains.HavingStats;
import mallorcatour.brains.neural.IPokerNeurals;
import mallorcatour.brains.stats.PokerStats;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class GusXensen implements IPokerNeurals, HavingStats {

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

	@Override
	public IPokerStats getStats() {
		return new PokerStats(0.79, 0.52, 0.37, 0.56);
	}

	@Override
	public String getName() {
		return "GusXensen";
	}
}
