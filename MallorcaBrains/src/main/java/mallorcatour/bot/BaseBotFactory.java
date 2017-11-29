package mallorcatour.bot;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.core.game.situation.observer.SituationHandler;

public abstract class BaseBotFactory implements IBotFactory {

    protected SituationHandler createHandler(boolean needFullPotentialOnFlop, String hero) {
        return new SituationHandler(needFullPotentialOnFlop, hero, new PokerEquilatorBrecher(), new PreflopEquilatorImpl());
    }
}
