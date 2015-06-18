package mallorcatour.brains.neural.cuba;

import mallorcatour.bot.IStudent;
import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.situation.SituationHandler;

public class CubaFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, IStudent student, String name, String debug) {
		Cuba player = new Cuba();
		NeuralAdvisor advisor = new NeuralAdvisor(player, player, "Cuba");
		Player realPlayer = new Player(IAdvisor.UNSUPPORTED, IAdvisor.UNSUPPORTED, advisor, IActionChecker.EMPTY, name,
				debug);
		SituationHandler handler = new SituationHandler(LimitType.NO_LIMIT, true, name);
		realPlayer.set(handler, handler, handler);
		return realPlayer;
	}

}
