package mallorcatour.bot.math;

import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.modeller.SpectrumPlayerObserver;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.GameObservers;
import mallorcatour.core.game.HoleCardsObservers;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.situation.handler.SituationHandler;

public class NLFullMathBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(villainModel, villainListener,
				strengthManager, spectrumListener, name, false);
		EVAdvisor evAdvisor = new EVAdvisor(villainModel, strengthManager, villainObserver, debug);

		Player player = new Player(IAdvisor.UNSUPPORTED, IAdvisor.UNSUPPORTED, evAdvisor, IActionChecker.EMPTY, name,
				debug);
		player.setStudent(heroListener);
		SituationHandler situationHandler = new SituationHandler(true, name);
		player.set(situationHandler, new GameObservers(situationHandler, villainObserver, strengthManager),
				new HoleCardsObservers(villainObserver, situationHandler));
		return player;
	}
}