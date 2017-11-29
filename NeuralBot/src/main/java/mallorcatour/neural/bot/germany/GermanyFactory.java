package mallorcatour.neural.bot.germany;

import mallorcatour.bot.Player;
import mallorcatour.bot.BaseBotFactory;
import mallorcatour.core.player.interfaces.IPlayer;
import mallorcatour.Advisor;
import mallorcatour.neural.bot.NeuralAdvisorImpl;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.game.situation.observer.SituationHandler;

public class GermanyFactory extends BaseBotFactory {

	@Override
	public IPlayer createBot(Advisor villainModel, ISpectrumListener spectrumListener,
							 AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		Germany neural = new Germany();
		NeuralAdvisorImpl advisor = new NeuralAdvisorImpl(neural, neural.getStats(), "Germany");
		Player player = new Player(Advisor.UNSUPPORTED, Advisor.UNSUPPORTED, advisor, name,
				debug);
		player.setStudent(heroListener);
		SituationHandler handler = createHandler(true, name);
		player.set(handler, handler, handler);
		return player;
	}

}
