package mallorcatour.bot;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.equilator.PokerEquilatorBrecher;
import mallorcatour.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.core.game.state.observer.StrengthHandStateObserver;

public abstract class BaseBotFactory implements IBotFactory {

    protected StrengthHandStateObserver createHandler(boolean needFullPotentialOnFlop, String hero) {
        return new StrengthHandStateObserver(needFullPotentialOnFlop, hero, new PokerEquilatorBrecher(), new PreflopEquilatorImpl());
    }
}
