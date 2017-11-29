package mallorcatour.brains.neural.pbx;

import mallorcatour.bot.Player;
import mallorcatour.brains.neural.BaseBotFactory;
import mallorcatour.core.player.interfaces.IPlayer;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.game.situation.observer.SituationHandler;

public class PbxFactory extends BaseBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		Pbx pbx = new Pbx();
		NeuralAdvisor advisor = new NeuralAdvisor(pbx, pbx, "PBX");
		Player player = new Player(IAdvisor.UNSUPPORTED, IAdvisor.UNSUPPORTED, advisor, IActionChecker.EMPTY, name,
				debug);
		player.setStudent(heroListener);
		SituationHandler handler = createHandler(true, name);
		player.set(handler, handler, handler);
		return player;
	}

}
