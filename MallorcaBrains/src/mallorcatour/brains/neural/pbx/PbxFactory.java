package mallorcatour.brains.neural.pbx;

import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.situation.handler.SituationHandler;

public class PbxFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		Pbx pbx = new Pbx();
		NeuralAdvisor advisor = new NeuralAdvisor(pbx, pbx, "PBX");
		Player player = new Player(IAdvisor.UNSUPPORTED, IAdvisor.UNSUPPORTED, advisor, IActionChecker.EMPTY, name,
				debug);
		player.setStudent(heroListener);
		SituationHandler handler = new SituationHandler(true, name);
		player.set(handler, handler, handler);
		return player;
	}

}
