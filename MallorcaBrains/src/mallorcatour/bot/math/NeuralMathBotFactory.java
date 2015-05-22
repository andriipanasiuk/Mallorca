package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IExternalAdvisor;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.modeller.SpectrumSituationHandler;
import mallorcatour.bot.neural.GrandtorinoBot;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.LimitType;

public class NeuralMathBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		StrengthManager manager = new StrengthManager(false);
		IProfitCalculator profitCalculator = new NLProfitCalculator(villainModel);
		IActionChecker actionChecker = new NLRiverActionChecker(profitCalculator, manager);
		GusXensen player = new GusXensen();
		LimitType limitType = LimitType.NO_LIMIT;
		SpectrumSituationHandler handler = new SpectrumSituationHandler(villainModel, limitType, true, true,
				spectrumListener, decisionListener, manager, debug);
		return new GrandtorinoBot(new NeuralAdvisor(player, player, "Gus Xensen"), handler, handler,
				actionChecker, limitType, IExternalAdvisor.EMPTY, false, debug);
	}
}