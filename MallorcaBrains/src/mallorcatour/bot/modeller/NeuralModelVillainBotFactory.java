package mallorcatour.bot.modeller;

import mallorcatour.bot.IStudent;
import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IActionObserver;
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
 * Creates bot that played with neural and model villain by him actions. During
 * the hand situation handler try to precise villain spectrum.
 * 
 * @author andriipanasiuk
 * 
 */
public class NeuralModelVillainBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener villainSpectrumListener,
			IDecisionListener decisionListener, IStudent student, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		GusXensen player = new GusXensen();
		Player bot = new Player(IAdvisor.UNSUPPORTED, new NLPreflopChart(), new NeuralAdvisor(player, player,
				"Gus Xensen"), IActionChecker.EMPTY, name, debug);
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(new NeuralAdvisor(player, player,
				"Gus Xensen Villain Model"), IActionObserver.EMPTY, strengthManager, villainSpectrumListener, name,
				false);
		SituationHandler handler = new SituationHandler(LimitType.NO_LIMIT, true, name);
		bot.set(handler, new GameObservers(handler, strengthManager, villainObserver), new HoleCardsObservers(
				villainObserver, handler));
		return bot;
	}
}