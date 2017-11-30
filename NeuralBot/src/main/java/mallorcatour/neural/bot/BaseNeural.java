package mallorcatour.neural.bot;

import org.neuroph.core.NeuralNetwork;

public abstract class BaseNeural implements IPokerNeurals {
    protected NeuralNetwork<?> preflop;
    protected NeuralNetwork<?> flop;
    protected NeuralNetwork<?> turn;
    protected NeuralNetwork<?> river;

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
