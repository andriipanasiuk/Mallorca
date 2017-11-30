package mallorcatour.neural.bot.gusxensen;

import mallorcatour.advice.creator.AggroAdviceCreator;
import mallorcatour.bot.BaseBotFactory;
import mallorcatour.bot.Player;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.state.observer.StrengthHandStateObserver;
import mallorcatour.core.player.interfaces.IPlayer;
import mallorcatour.neural.bot.NeuralAdvisorImpl;

public class GusXensenAggressiveFactory extends BaseBotFactory {

	@Override
	public IPlayer createBot(AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		GusXensen player = new GusXensen();
		NeuralAdvisorImpl advisor = new NeuralAdvisorImpl(player, player.getStats(), "Gus Xensen", new AggroAdviceCreator());
		Player realPlayer = new Player(new NLPreflopChart(), advisor, name,
				debug);
		StrengthHandStateObserver handler = createHandler(true, name);
		realPlayer.set(handler, handler, handler);
		return realPlayer;
	}

}
