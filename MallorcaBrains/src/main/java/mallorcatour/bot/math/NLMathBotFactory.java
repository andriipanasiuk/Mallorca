package mallorcatour.bot.math;

import mallorcatour.bot.Bot;
import mallorcatour.core.game.state.observer.StrengthHandStateObserver;
import mallorcatour.bot.SpectrumPlayerObserver;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.bot.BaseBotFactory;
import mallorcatour.core.game.GameObservers;
import mallorcatour.core.game.HoleCardsObservers;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.player.interfaces.Player;
import mallorcatour.equilator.PokerEquilatorBrecher;
import mallorcatour.equilator.preflop.PreflopEquilatorImpl;

public class NLMathBotFactory extends BaseBotFactory {

	private ISpectrumListener spectrumListener;
	private final Advisor villainModel;
	private final Advisor preflopAdvisor;

	public NLMathBotFactory(Advisor villainModel, Advisor preflopAdvisor) {
		this.villainModel = villainModel;
		this.preflopAdvisor = preflopAdvisor;
	}

	public NLMathBotFactory(Advisor villainModel) {
		this(villainModel, Advisor.UNSUPPORTED);
	}

	public void setSpectrumListener(ISpectrumListener spectrumListener) {
		this.spectrumListener = spectrumListener;
	}

	@Override
	public Player createBot(AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false, new PokerEquilatorBrecher(), new PreflopEquilatorImpl());
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(villainModel, villainListener,
				strengthManager, name, false);
		EVAdvisor evAdvisor = new EVAdvisor(villainModel, strengthManager, villainObserver, debug);

		StrengthHandStateObserver stateObserver = createHandler(true, name);
		Bot bot = new Bot(preflopAdvisor, evAdvisor, stateObserver, name,
				debug);
		bot.setAdvisorListener(heroListener);
		bot.set(new GameObservers(stateObserver, villainObserver, strengthManager),
				new HoleCardsObservers(villainObserver, stateObserver));
		return bot;
	}
}