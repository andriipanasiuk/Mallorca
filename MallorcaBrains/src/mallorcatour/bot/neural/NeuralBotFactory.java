package mallorcatour.bot.neural;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.interfaces.IVillainModel;
import mallorcatour.bot.math.IVillainSpectrumHandler;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.situation.SituationHandler;
import mallorcatour.robot.humanadvisor.IHumanAdvisor;

public class NeuralBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IVillainModel villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		GusXensen player = new GusXensen();
		SituationHandler handler = new SituationHandler(LimitType.NO_LIMIT, true);
		return new GrandtorinoBot(new NeuralAdvisor(player, player, "Gus Xensen"), villainModel, handler,
				IVillainSpectrumHandler.DEFAULT, IActionChecker.EMPTY, LimitType.NO_LIMIT, IHumanAdvisor.EMPTY, false,
				debug);
	}

}
