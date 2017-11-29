package mallorcatour.brains.neural.dafish2;

import mallorcatour.bot.Player;
import mallorcatour.brains.neural.BaseBotFactory;
import mallorcatour.core.player.interfaces.IPlayer;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.game.situation.observer.SituationHandler;

public class Dafish2Factory extends BaseBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		Dafish2 dafish = new Dafish2();
		NeuralAdvisor advisor = new NeuralAdvisor(dafish, dafish, "DaFish2");
		Player player = new Player(IAdvisor.UNSUPPORTED, IAdvisor.UNSUPPORTED, advisor, IActionChecker.EMPTY, name,
				debug);
		player.setStudent(heroListener);
		SituationHandler handler = createHandler(true, name);
		player.set(handler, handler, handler);
		return player;
	}

}
