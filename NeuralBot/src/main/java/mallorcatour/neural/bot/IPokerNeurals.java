package mallorcatour.neural.bot;

import mallorcatour.core.player.interfaces.HasName;
import mallorcatour.stats.PokerStats;

import org.neuroph.core.NeuralNetwork;

public interface IPokerNeurals /*extends HasName*/ {

	NeuralNetwork<?> getRiver();

	NeuralNetwork<?> getTurn();

	NeuralNetwork<?> getFlop();

	NeuralNetwork<?> getPreflop();

	PokerStats getStats();

}