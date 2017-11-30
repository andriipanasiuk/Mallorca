package mallorcatour.game;

import mallorcatour.bot.math.NLMathBotFactory;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.advice.UnitedAdvisor;
import mallorcatour.neural.bot.NeuralAdvisorImpl;
import mallorcatour.neural.bot.gusxensen.GusXensen;

/**
 * Create bot with GusXensen + chart preflop and decision-tree postflop.
 */
@SuppressWarnings("unused")
public class NLPostflopMathBotFactory extends NLMathBotFactory {

    public NLPostflopMathBotFactory(Advisor villainModel) {
        super(villainModel, new UnitedAdvisor(new NLPreflopChart(),
                new NeuralAdvisorImpl(new GusXensen(), new GusXensen().getStats(), "Gus Xensen")));
    }
}
