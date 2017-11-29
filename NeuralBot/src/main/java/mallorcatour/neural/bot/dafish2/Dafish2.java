package mallorcatour.neural.bot.dafish2;

import mallorcatour.neural.bot.IPokerNeurals;
import mallorcatour.stats.IPokerStats;
import mallorcatour.stats.PokerStats;
import mallorcatour.neural.core.NeuralCreator;

import org.neuroph.core.NeuralNetwork;

public class Dafish2 implements IPokerNeurals {

	private NeuralNetwork<?> preflop;
	private NeuralNetwork<?> flop;
	private NeuralNetwork<?> turn;
	private NeuralNetwork<?> river;

	public Dafish2() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralDafish2());
		flop = NeuralCreator.createPerceptron(new FlopNeuralDafish2());
		turn = NeuralCreator.createPerceptron(new TurnNeuralDafish2());
		river = NeuralCreator.createPerceptron(new RiverNeuralDafish2());
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
		return new PokerStats(0.66, 0.53, 0.35, 0.62);
	}

	@Override
	public String getName() {
		return "DaFish2";
	}

}
