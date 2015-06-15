package mallorcatour.bot.neural;

import mallorcatour.bot.Player;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.advice.AggroAdviceCreator;
import mallorcatour.core.game.situation.SituationHandler;

public class NeuralAggroBotFactory implements IBotFactory {

	@Override
	public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String name, String debug) {
		GusXensen player = new GusXensen();
		NeuralAdvisor advisor = new NeuralAdvisor(player, player, "Gus Xensen", new AggroAdviceCreator());
		Player realPlayer = new Player(IAdvisor.UNSUPPORTED, new NLPreflopChart(), advisor, IActionChecker.EMPTY, name,
				debug);
		SituationHandler handler = new SituationHandler(LimitType.NO_LIMIT, true, name);
		realPlayer.set(handler, handler, handler);
		return realPlayer;
	}

}
