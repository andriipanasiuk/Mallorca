package mallorcatour.brains.neural;

import org.neuroph.core.NeuralNetwork;

public interface IPokerNeurals {

	NeuralNetwork<?> getRiver();

	NeuralNetwork<?> getTurn();

	NeuralNetwork<?> getFlop();

	NeuralNetwork<?> getPreflop();

}