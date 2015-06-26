package mallorcatour.brains.neural.dafish;

import mallorcatour.brains.HavingStats;
import mallorcatour.brains.neural.IPokerNeurals;
import mallorcatour.brains.stats.PokerStats;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class Dafish implements IPokerNeurals, HavingStats {

	private NeuralNetwork<?> preflop;
	private NeuralNetwork<?> flop;
	private NeuralNetwork<?> turn;
	private NeuralNetwork<?> river;

	public Dafish() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralDafish());
		flop = NeuralCreator.createPerceptron(new FlopNeuralDafish());
		turn = NeuralCreator.createPerceptron(new TurnNeuralDafish());
		river = NeuralCreator.createPerceptron(new RiverNeuralDafish());
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
		return new PokerStats(0.83, 0.72, 0.33, 0.74);
	}

	@Override
	public String getName() {
		return "DaFish";
	}

}
