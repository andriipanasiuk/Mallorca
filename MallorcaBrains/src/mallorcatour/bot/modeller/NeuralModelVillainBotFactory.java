package mallorcatour.bot.modeller;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.interfaces.IVillainModel;
import mallorcatour.bot.neural.GrandtorinoBot;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.LimitType;
import mallorcatour.robot.humanadvisor.IHumanAdvisor;

/**
 * Creates bot that played with neural and model villain by him actions. During
 * the hand situation handler try to precise villain spectrum.
 * 
 * @author andriipanasiuk
 * 
 */
public class NeuralModelVillainBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IVillainModel villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		StrengthManager strengthManager = new StrengthManager(false);
		SpectrumSituationHandler handler = new SpectrumSituationHandler(villainModel, LimitType.NO_LIMIT, true, true,
				spectrumListener, decisionListener, strengthManager, true, debug);
		GusXensen player = new GusXensen();
		return new GrandtorinoBot(new NeuralAdvisor(player, player, "Gus Xensen"), villainModel, handler, handler,
				IActionChecker.EMPTY, LimitType.NO_LIMIT, IHumanAdvisor.EMPTY, false, debug);
	}

}