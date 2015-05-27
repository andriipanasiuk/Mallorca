package mallorcatour.bot.modeller;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.neural.GameObservers;
import mallorcatour.bot.neural.GrandtorinoBot;
import mallorcatour.bot.neural.HoleCardsObservers;
import mallorcatour.bot.neural.SpectrumPlayerObserver;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.situation.SituationHandler;

/**
 * Creates bot that played with neural and model villain by him actions. During
 * the hand situation handler try to precise villain spectrum.
 * 
 * @author andriipanasiuk
 * 
 */
public class NeuralModelVillainBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener villainSpectrumListener,
			IDecisionListener decisionListener, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		GusXensen player = new GusXensen();
		GrandtorinoBot bot = new GrandtorinoBot(new NeuralAdvisor(player, player, "Gus Xensen"), LimitType.NO_LIMIT,
				debug);
		bot.setName(name);
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(new NeuralAdvisor(player, player,
				"Gus Xensen Villain Model"), strengthManager, villainSpectrumListener, bot.getName(), false);
		SituationHandler handler = new SituationHandler(LimitType.NO_LIMIT, true, name);
		bot.set(new GameObservers(handler, strengthManager, villainObserver), handler, new HoleCardsObservers(
				villainObserver, handler));
		bot.set(IActionChecker.EMPTY);
		bot.set(villainObserver);
		bot.setName(name);
		return bot;
	}
}