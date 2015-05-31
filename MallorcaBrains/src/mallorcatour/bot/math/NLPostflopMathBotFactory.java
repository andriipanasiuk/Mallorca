package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.bot.villainobserver.SpectrumPlayerObserver;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.GameObservers;
import mallorcatour.core.game.HoleCardsObservers;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.situation.SituationHandler;

public class NLPostflopMathBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String name, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		IProfitCalculator profitCalculator = new NLProfitCalculator(villainModel, strengthManager);
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(villainModel, strengthManager,
				spectrumListener, name, false);

		GusXensen player = new GusXensen();
		IAdvisor preflopAdvisor = new NeuralAdvisor(player, player, "Gus Xensen");
		IAdvisor chart = new NLPreflopChart();

		NLMathBot bot = new NLMathBot(preflopAdvisor, chart, profitCalculator, name, debug, villainObserver);
		SituationHandler situationHandler = new SituationHandler(LimitType.NO_LIMIT, true, name);
		bot.set(situationHandler, new GameObservers(situationHandler, villainObserver, strengthManager),
				new HoleCardsObservers(villainObserver, situationHandler));
		return bot;
	}
}