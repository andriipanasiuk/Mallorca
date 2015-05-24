package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.modeller.SpectrumSituationHandler;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.LimitType;

public class NLMathBotFactory implements IBotFactory{

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		IProfitCalculator profitCalculator = new NLProfitCalculator(villainModel, strengthManager);
		SpectrumSituationHandler situationHandler = new SpectrumSituationHandler(villainModel, LimitType.NO_LIMIT,
				true, true, spectrumListener, decisionListener, strengthManager, true, debug);
		return new NLMathBot(profitCalculator, situationHandler, situationHandler, debug);
	}
}