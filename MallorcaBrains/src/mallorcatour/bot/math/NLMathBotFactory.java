package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.neural.SpectrumPlayerObserver;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.situation.SituationHandler;

public class NLMathBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		IProfitCalculator profitCalculator = new NLProfitCalculator(villainModel, strengthManager);
		SituationHandler situationHandler = new SituationHandler(LimitType.NO_LIMIT, true);
		SpectrumPlayerObserver villainObserver = new SpectrumPlayerObserver(villainModel, strengthManager,
				spectrumListener);
		return new NLMathBot(profitCalculator, situationHandler, villainObserver, debug);
	}
}