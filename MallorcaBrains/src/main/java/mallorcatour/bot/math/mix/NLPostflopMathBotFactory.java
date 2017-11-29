package mallorcatour.bot.math.mix;

import mallorcatour.bot.Player;
import mallorcatour.bot.math.EVAdvisor;
import mallorcatour.bot.math.StrengthManager;
import mallorcatour.bot.modeller.PlayerStatModel;
import mallorcatour.bot.modeller.SpectrumPlayerObserver;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.BaseBotFactory;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.core.game.GameObservers;
import mallorcatour.core.game.HoleCardsObservers;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.game.situation.observer.SituationHandler;
import mallorcatour.core.player.interfaces.IPlayer;

/**
 * Create bot with GusXensen + chart preflop and decision-tree postflop.
 *
 */
public class NLPostflopMathBotFactory extends BaseBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false, new PokerEquilatorBrecher(), new PreflopEquilatorImpl());
		PlayerStatModel model = new PlayerStatModel(debug);
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(model, model, strengthManager,
				spectrumListener, name, false);

		EVAdvisor evAdvisor = new EVAdvisor(villainModel, strengthManager, villainObserver, debug);
		GusXensen player = new GusXensen();
		IAdvisor preflopAdvisor = new NeuralAdvisor(player, player, "Gus Xensen");
		IAdvisor chart = new NLPreflopChart();

		Player bot = new Player(preflopAdvisor, chart, evAdvisor, IActionChecker.EMPTY, name, debug);
		SituationHandler situationHandler = createHandler(true, name);
		bot.set(situationHandler, new GameObservers(situationHandler, villainObserver, strengthManager),
				new HoleCardsObservers(villainObserver, situationHandler));
		return bot;
	}
}