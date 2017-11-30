package mallorcatour.neural.bot;

import mallorcatour.stats.PokerStats;

import org.neuroph.core.NeuralNetwork;

public interface IPokerNeurals {

	NeuralNetwork<?> getRiver();

	NeuralNetwork<?> getTurn();

	NeuralNetwork<?> getFlop();

	NeuralNetwork<?> getPreflop();

	PokerStats getStats();

}