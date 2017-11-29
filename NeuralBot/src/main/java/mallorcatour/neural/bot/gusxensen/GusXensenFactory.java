package mallorcatour.neural.bot.gusxensen;

import mallorcatour.bot.Player;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.Advisor;
import mallorcatour.neural.bot.NeuralAdvisorImpl;
import mallorcatour.bot.BaseBotFactory;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.game.situation.observer.SituationHandler;
import mallorcatour.core.player.interfaces.IPlayer;

public class GusXensenFactory extends BaseBotFactory {

	@Override
	public IPlayer createBot(Advisor villainModel, ISpectrumListener spectrumListener,
							 AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		GusXensen player = new GusXensen();
		NeuralAdvisorImpl advisor = new NeuralAdvisorImpl(player, player.getStats(), "Gus Xensen");
		Player realPlayer = new Player(Advisor.UNSUPPORTED, new NLPreflopChart(), advisor, name,
				debug);
		SituationHandler handler = createHandler(true, name);
		realPlayer.set(handler, handler, handler);
		return realPlayer;
	}

}
