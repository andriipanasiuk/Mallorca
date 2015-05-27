package mallorcatour.bot.math;

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
 * Creates neural bot with river's action profit check by decision-tree.
 * 
 * @author andriipanasiuk
 * 
 */
public class NeuralMathBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		IProfitCalculator profitCalculator = new NLProfitCalculator(villainModel, strengthManager);
		GusXensen player = new GusXensen();
		LimitType limitType = LimitType.NO_LIMIT;
		GrandtorinoBot bot = new GrandtorinoBot(new NeuralAdvisor(player, player, "Gus Xensen"),
				limitType, debug);
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(villainModel, strengthManager,
				spectrumListener, bot.getName(), false);
		IActionChecker actionChecker = new NLRiverActionChecker(profitCalculator, villainObserver);
		SituationHandler handler = new SituationHandler(limitType, true, bot.getName());
		bot.set(new GameObservers(strengthManager, handler, villainObserver), handler, new HoleCardsObservers(
				villainObserver, handler));
		bot.set(villainObserver);
		bot.set(actionChecker);
		return bot;
	}
}