package mallorcatour.bot.math;

import mallorcatour.bot.IStudent;
import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.modeller.PlayerStatModel;
import mallorcatour.bot.villainobserver.SpectrumPlayerObserver;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.GameObservers;
import mallorcatour.core.game.HoleCardsObservers;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.situation.SituationHandler;

public class NLFullMathBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, IStudent student, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		PlayerStatModel model = new PlayerStatModel(debug);
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(model, model, strengthManager,
				spectrumListener, name, false);
		EVAdvisor evAdvisor = new EVAdvisor(villainModel, strengthManager, villainObserver, debug);

		Player bot = new Player(IAdvisor.UNSUPPORTED, IAdvisor.UNSUPPORTED, evAdvisor, IActionChecker.EMPTY, name,
				debug);
		bot.setStudent(student);
		SituationHandler situationHandler = new SituationHandler(LimitType.NO_LIMIT, true, name);
		bot.set(situationHandler, new GameObservers(situationHandler, villainObserver, strengthManager),
				new HoleCardsObservers(villainObserver, situationHandler));
		return bot;
	}
}