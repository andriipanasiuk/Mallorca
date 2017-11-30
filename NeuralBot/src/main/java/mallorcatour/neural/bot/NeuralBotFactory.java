package mallorcatour.neural.bot;

import mallorcatour.bot.BaseBotFactory;
import mallorcatour.bot.Player;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.state.observer.StrengthHandStateObserver;
import mallorcatour.core.player.interfaces.IPlayer;

public class NeuralBotFactory extends BaseBotFactory {

    private final IPokerNeurals pokerNeurals;
    private final String neuralName;

    public NeuralBotFactory(IPokerNeurals pokerNeurals, String neuralName) {
        this.pokerNeurals = pokerNeurals;
        this.neuralName = neuralName;
    }

    @Override
    public IPlayer createBot(AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
        NeuralAdvisorImpl advisor = new NeuralAdvisorImpl(pokerNeurals, pokerNeurals.getStats(), neuralName);
        Player player = new Player(Advisor.UNSUPPORTED, advisor, name,
                debug);
        player.setStudent(heroListener);
        StrengthHandStateObserver handler = createHandler(true, name);
        player.set(handler, handler, handler);
        return player;
    }
}
