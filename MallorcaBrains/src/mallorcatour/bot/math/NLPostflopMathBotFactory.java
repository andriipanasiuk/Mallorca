package mallorcatour.bot.math;

import mallorcatour.bot.AdvisorListener;
import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.modeller.PlayerStatModel;
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
 * Create bot with GusXensen + chart preflop and decision-tree postflop.
 *
 */
public class NLPostflopMathBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		PlayerStatModel model = new PlayerStatModel(debug);
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(model, model, strengthManager,
				spectrumListener, name, false);

		EVAdvisor evAdvisor = new EVAdvisor(villainModel, strengthManager, villainObserver, debug);
		GusXensen player = new GusXensen();
		IAdvisor preflopAdvisor = new NeuralAdvisor(player, player, "Gus Xensen");
		IAdvisor chart = new NLPreflopChart();

		Player bot = new Player(preflopAdvisor, chart, evAdvisor, IActionChecker.EMPTY, name, debug);
		SituationHandler situationHandler = new SituationHandler(LimitType.NO_LIMIT, true, name);
		bot.set(situationHandler, new GameObservers(situationHandler, villainObserver, strengthManager),
				new HoleCardsObservers(villainObserver, situationHandler));
		return bot;
	}
}