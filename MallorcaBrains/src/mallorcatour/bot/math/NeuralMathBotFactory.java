package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.bot.villainobserver.SpectrumPlayerObserver;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.GameObservers;
import mallorcatour.core.game.HoleCardsObservers;
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
			IDecisionListener decisionListener, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		IProfitCalculator profitCalculator = new NLProfitCalculator(villainModel, strengthManager);
		GusXensen player = new GusXensen();
		LimitType limitType = LimitType.NO_LIMIT;
		IAdvisor commonAdvisor = new NeuralAdvisor(player, player, "Gus Xensen");
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(villainModel, strengthManager,
				spectrumListener, name, false);
		IActionChecker actionChecker = new NLRiverActionChecker(profitCalculator, villainObserver);
		Player bot = new Player(IAdvisor.UNSUPPORTED, new NLPreflopChart(), commonAdvisor, actionChecker, name, debug);

		SituationHandler handler = new SituationHandler(limitType, true, name);
		bot.set(handler, new GameObservers(strengthManager, handler, villainObserver), new HoleCardsObservers(
				villainObserver, handler));
		return bot;
	}
}