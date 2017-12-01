package mallorcatour.neural.bot.gusxensen;

import mallorcatour.neural.bot.BaseNeural;
import mallorcatour.neural.core.NeuralCreator;
import mallorcatour.core.stats.PokerStats;
import mallorcatour.core.stats.PokerStatsImpl;

public class GusXensen extends BaseNeural {

    public GusXensen() {
        preflop = NeuralCreator.createPerceptron(new PreflopNeuralGX());
        flop = NeuralCreator.createPerceptron(new FlopNeuralGX());
        turn = NeuralCreator.createPerceptron(new TurnNeuralGX());
        river = NeuralCreator.createPerceptron(new RiverNeuralGX());
    }

    public PokerStats getStats() {
        return new PokerStatsImpl(0.79, 0.52, 0.37, 0.56);
    }

    @Override
    public String toString() {
        return "GusXensen Neural";
    }
}
