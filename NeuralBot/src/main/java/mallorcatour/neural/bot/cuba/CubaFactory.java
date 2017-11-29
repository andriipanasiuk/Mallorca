package mallorcatour.neural.bot.cuba;

import mallorcatour.bot.Player;
import mallorcatour.bot.BaseBotFactory;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.core.player.interfaces.IPlayer;
import mallorcatour.Advisor;
import mallorcatour.neural.bot.NeuralAdvisorImpl;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.game.situation.observer.SituationHandler;

public class CubaFactory extends BaseBotFactory {

	@Override
	public IPlayer createBot(Advisor villainModel, ISpectrumListener spectrumListener,
							 AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
		Cuba cuba = new Cuba();
		NeuralAdvisorImpl advisor = new NeuralAdvisorImpl(cuba, cuba.getStats(), "Cuba");
		Player player = new Player(Advisor.UNSUPPORTED, Advisor.UNSUPPORTED, advisor, name,
				debug);
		player.setStudent(heroListener);
		SituationHandler handler = new SituationHandler(true, name, new PokerEquilatorBrecher(), new PreflopEquilatorImpl());
		player.set(handler, handler, handler);
		return player;
	}

}
