package mallorcatour.bot.math;

import mallorcatour.bot.Player;
import mallorcatour.bot.modeller.SpectrumPlayerObserver;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.BaseBotFactory;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.core.game.GameObservers;
import mallorcatour.core.game.HoleCardsObservers;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.game.situation.observer.SituationHandler;
import mallorcatour.core.player.interfaces.IPlayer;

public class NLFullMathBotFactory extends BaseBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false, new PokerEquilatorBrecher(), new PreflopEquilatorImpl());
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(villainModel, villainListener,
				strengthManager, spectrumListener, name, false);
		EVAdvisor evAdvisor = new EVAdvisor(villainModel, strengthManager, villainObserver, debug);

		Player player = new Player(IAdvisor.UNSUPPORTED, IAdvisor.UNSUPPORTED, evAdvisor, IActionChecker.EMPTY, name,
				debug);
		player.setStudent(heroListener);
		SituationHandler situationHandler = createHandler(true, name);
		player.set(situationHandler, new GameObservers(situationHandler, villainObserver, strengthManager),
				new HoleCardsObservers(villainObserver, situationHandler));
		return player;
	}
}