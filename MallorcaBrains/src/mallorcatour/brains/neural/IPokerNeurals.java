package mallorcatour.brains.neural;

import org.neuroph.core.NeuralNetwork;

public interface IPokerNeurals{

	public abstract NeuralNetwork<?> getRiver();

	public abstract NeuralNetwork<?> getTurn();

	public abstract NeuralNetwork<?> getFlop();

	public abstract NeuralNetwork<?> getPreflop();
	
}