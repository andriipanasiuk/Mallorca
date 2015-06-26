package mallorcatour.brains.neural.gusxensen;

import mallorcatour.bot.AdvisorListener;
import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.core.game.situation.SituationHandler;

public class GusXensenFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		GusXensen player = new GusXensen();
		NeuralAdvisor advisor = new NeuralAdvisor(player, player, "Gus Xensen");
		Player realPlayer = new Player(IAdvisor.UNSUPPORTED, new NLPreflopChart(), advisor, IActionChecker.EMPTY, name,
				debug);
		SituationHandler handler = new SituationHandler(true, name);
		realPlayer.set(handler, handler, handler);
		return realPlayer;
	}

}
