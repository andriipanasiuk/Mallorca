package mallorcatour.neural.bot;

import mallorcatour.bot.BaseBotFactory;
import mallorcatour.bot.Bot;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.state.observer.StrengthHandStateObserver;
import mallorcatour.core.player.interfaces.Player;

public class NeuralBotFactory extends BaseBotFactory {

    private final IPokerNeurals pokerNeurals;
    private final String neuralName;

    public NeuralBotFactory(IPokerNeurals pokerNeurals, String neuralName) {
        this.pokerNeurals = pokerNeurals;
        this.neuralName = neuralName;
    }

    @Override
    public Player createBot(AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
        NeuralAdvisorImpl advisor = new NeuralAdvisorImpl(pokerNeurals, pokerNeurals.getStats(), neuralName);
        StrengthHandStateObserver stateObserver = createHandler(true, name);
        Bot bot = new Bot(Advisor.UNSUPPORTED, advisor, stateObserver, name,
                debug);
        bot.setAdvisorListener(heroListener);
        bot.set(stateObserver, stateObserver);
        return bot;
    }
}
