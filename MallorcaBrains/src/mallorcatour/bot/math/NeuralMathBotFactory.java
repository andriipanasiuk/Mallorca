package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.interfaces.IVillainModeller;
import mallorcatour.bot.neural.GrandtorinoBot;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.StrengthManager;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensenNeural;
import mallorcatour.core.game.LimitType;
import mallorcatour.robot.humanadvisor.IHumanAdvisor;

public class NeuralMathBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IVillainModeller modeller, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		StrengthManager manager = new StrengthManager(false);
		IProfitCalculator profitCalculator = new NLProfitCalculator(modeller);
		IActionChecker actionChecker = new NLRiverActionChecker(profitCalculator, manager);
		return new GrandtorinoBot(new NeuralAdvisor(new GusXensenNeural()), modeller, actionChecker, manager,
				LimitType.NO_LIMIT, spectrumListener, decisionListener, IHumanAdvisor.EMPTY, false, true, true,
				debug);
	}
}