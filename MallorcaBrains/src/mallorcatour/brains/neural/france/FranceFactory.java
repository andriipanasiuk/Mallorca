package mallorcatour.brains.neural.france;

import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.situation.handler.SituationHandler;

public class FranceFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		France france = new France();
		NeuralAdvisor advisor = new NeuralAdvisor(france, france, "France");
		Player player = new Player(IAdvisor.UNSUPPORTED, IAdvisor.UNSUPPORTED, advisor, IActionChecker.EMPTY, name,
				debug);
		player.setStudent(heroListener);
		SituationHandler handler = new SituationHandler(true, name);
		player.set(handler, handler, handler);
		return player;
	}

}
