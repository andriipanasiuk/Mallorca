package mallorcatour.bot.modeller;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IExternalAdvisor;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.neural.GrandtorinoBot;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.LimitType;

/**
 * Creates bot that played with neural and model villain by him actions. During
 * the hand situation handler try to precise villain spectrum.
 * 
 * @author andriipanasiuk
 * 
 */
public class NeuralModelVillainBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		SpectrumSituationHandler handler = new SpectrumSituationHandler(villainModel, LimitType.NO_LIMIT, true, true,
				spectrumListener, decisionListener, strengthManager, true, debug);
		GusXensen player = new GusXensen();
		return new GrandtorinoBot(new NeuralAdvisor(player, player, "Gus Xensen"), handler, handler,
				IActionChecker.EMPTY, LimitType.NO_LIMIT, IExternalAdvisor.EMPTY, false, debug);
	}

}