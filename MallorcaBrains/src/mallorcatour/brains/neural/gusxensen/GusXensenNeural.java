package mallorcatour.brains.neural.gusxensen;

import org.neuroph.core.NeuralNetwork;

import mallorcatour.brains.neural.IPokerNeurals;
import mallorcatour.neural.core.NeuralCreator;

public class GusXensenNeural implements IPokerNeurals {

	private NeuralNetwork<?> preflop, flop, turn, river;

	public GusXensenNeural() {
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

}
