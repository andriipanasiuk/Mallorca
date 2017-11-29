package mallorcatour.bot.sklansky;

import mallorcatour.bot.ObservingPlayer;
import mallorcatour.core.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.interfaces.IGameObserver;

public class PushBot extends ObservingPlayer {

	public PushBot(String name, String debug) {
		super(name, debug);
		set(null, IGameObserver.EMPTY, IHoleCardsObserver.EMPTY);
	}

	@Override
	public Action getAction() {
		double strength = PreflopEquilatorImpl.strengthVsRandom(heroCard1, heroCard2);
		if (strength > 0.5) {
			return Action.allIn();
		} else {
			return Action.fold();
		}
	}

	@Override
	public String getDefaultName() {
		return "RandomBot";
	}

}
