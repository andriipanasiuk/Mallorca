package mallorcatour.brains.neural.checkburn;

import mallorcatour.brains.HavingStats;
import mallorcatour.brains.neural.IPokerNeurals;
import mallorcatour.brains.stats.PokerStats;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class CheckBurn implements IPokerNeurals, HavingStats {

	private NeuralNetwork<?> preflop, flop, turn, river;

	public CheckBurn() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralCCB());
		flop = NeuralCreator.createPerceptron(new FlopNeuralCCB());
		turn = NeuralCreator.createPerceptron(new TurnNeuralCCB());
		river = NeuralCreator.createPerceptron(new RiverNeuralCCB());
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
		return new PokerStats(0.5, 0.1, 0.29, 0.67);
	}

	@Override
	public String getName() {
		return "CheckCheckBurn";
	}

}
