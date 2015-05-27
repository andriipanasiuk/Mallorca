package mallorcatour.bot.neural;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.situation.SituationHandler;

public class NeuralBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		GusXensen player = new GusXensen();
		NeuralAdvisor advisor = new NeuralAdvisor(player, player, "Gus Xensen");
		GrandtorinoBot realPlayer = new GrandtorinoBot(advisor, LimitType.NO_LIMIT, debug);
		SituationHandler handler = new SituationHandler(LimitType.NO_LIMIT, true, realPlayer.getName());
		realPlayer.set(handler, handler, handler);
		realPlayer.set(IActionChecker.EMPTY);
		return realPlayer;
	}

}
