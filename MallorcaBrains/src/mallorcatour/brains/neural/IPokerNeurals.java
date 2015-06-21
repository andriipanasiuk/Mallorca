package mallorcatour.brains.neural;

import mallorcatour.bot.interfaces.HasName;

import org.neuroph.core.NeuralNetwork;

public interface IPokerNeurals extends HasName {

	NeuralNetwork<?> getRiver();

	NeuralNetwork<?> getTurn();

	NeuralNetwork<?> getFlop();

	NeuralNetwork<?> getPreflop();

}