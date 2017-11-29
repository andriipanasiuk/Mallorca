package mallorcatour.brains.neural.gusxensen;

import mallorcatour.bot.Player;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.BaseBotFactory;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.game.situation.observer.SituationHandler;
import mallorcatour.core.player.interfaces.IPlayer;

public class GusXensenFactory extends BaseBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		GusXensen player = new GusXensen();
		NeuralAdvisor advisor = new NeuralAdvisor(player, player, "Gus Xensen");
		Player realPlayer = new Player(IAdvisor.UNSUPPORTED, new NLPreflopChart(), advisor, IActionChecker.EMPTY, name,
				debug);
		SituationHandler handler = createHandler(true, name);
		realPlayer.set(handler, handler, handler);
		return realPlayer;
	}

}
