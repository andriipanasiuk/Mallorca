package mallorcatour.brains.neural.germany;

import mallorcatour.bot.IStudent;
import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.cuba.Cuba;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.situation.SituationHandler;

public class GermanyFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, IStudent student, String name, String debug) {
		Cuba cuba = new Cuba();
		NeuralAdvisor advisor = new NeuralAdvisor(cuba, cuba, "Cuba");
		Player player = new Player(IAdvisor.UNSUPPORTED, IAdvisor.UNSUPPORTED, advisor, IActionChecker.EMPTY, name,
				debug);
		player.setStudent(student);
		SituationHandler handler = new SituationHandler(LimitType.NO_LIMIT, true, name);
		player.set(handler, handler, handler);
		return player;
	}

}
