package mallorcatour.brains.neural.gusxensen;

import org.neuroph.core.NeuralNetwork;

import mallorcatour.brains.neural.IPokerNeurals;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.neural.core.NeuralCreator;

public class GusXensen implements IPokerNeurals, IPokerStats {

	private NeuralNetwork<?> preflop, flop, turn, river;

	public GusXensen() {
		preflop = NeuralCreator.createPerceptron(new PreflopNeuralInfo());
		flop = NeuralCreator.createPerceptron(new FlopNeuralInfo());
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

	@Override
	public double getAggressionFactor() {
		return 3.0;
	}

	@Override
	public double getWtsd() {
		return 0.4;
	}

	@Override
	public double getAggressionFrequency() {
		return 0.5;
	}

	@Override
	public double getFoldFrequency() {
		return 0.3;
	}

}
