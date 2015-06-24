package mallorcatour.brains.neural.france;

import mallorcatour.brains.HavingStats;
import mallorcatour.brains.neural.IPokerNeurals;
import mallorcatour.brains.stats.PokerStats;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class France implements IPokerNeurals, HavingStats {

	private NeuralNetwork<?> preflop, flop, turn, river;

	public France() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeural());
		flop = NeuralCreator.createPerceptron(new FlopNeural());
		turn = NeuralCreator.createPerceptron(new TurnNeural());
		river = NeuralCreator.createPerceptron(new RiverNeural());
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
		return new PokerStats(0.79, 0.53, 0.37, 0.24);
	}

	@Override
	public String getName() {
		return "France";
	}
}
