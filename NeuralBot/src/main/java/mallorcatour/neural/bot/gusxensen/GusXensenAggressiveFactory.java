package mallorcatour.neural.bot.gusxensen;

import mallorcatour.advice.creator.AggroAdviceCreator;
import mallorcatour.bot.BaseBotFactory;
import mallorcatour.bot.Bot;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.state.observer.StrengthHandStateObserver;
import mallorcatour.core.player.interfaces.Player;
import mallorcatour.neural.bot.NeuralAdvisorImpl;

public class GusXensenAggressiveFactory extends BaseBotFactory {

	@Override
	public Player createBot(AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		GusXensen player = new GusXensen();
		NeuralAdvisorImpl advisor = new NeuralAdvisorImpl(player, player.getStats(), "Gus Xensen", new AggroAdviceCreator());
		StrengthHandStateObserver stateObserver = createHandler(true, name);
		Bot bot = new Bot(new NLPreflopChart(), advisor, stateObserver, name,
				debug);
		bot.set(stateObserver, stateObserver);
		return bot;
	}

}
