package mallorcatour.neural.bot.checkburn;

import mallorcatour.neural.bot.IPokerNeurals;
import mallorcatour.stats.PokerStats;
import mallorcatour.stats.PokerStatsImpl;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class CheckBurn implements IPokerNeurals {

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

	public PokerStats getStats() {
		return new PokerStatsImpl(0.5, 0.1, 0.29, 0.67);
	}

	@Override
	public String getName() {
		return "CheckCheckBurn";
	}

}
