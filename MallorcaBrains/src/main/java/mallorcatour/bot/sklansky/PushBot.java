package mallorcatour.bot.sklansky;

import mallorcatour.bot.ObservingPlayer;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.PreflopEquilator;
import mallorcatour.core.player.interfaces.Player;
import mallorcatour.equilator.preflop.PreflopEquilatorImpl;

public class PushBot extends ObservingPlayer implements Player {

	private final PreflopEquilator preflopEquilator = new PreflopEquilatorImpl();

	public PushBot(String name, String debug) {
		super(name, debug);
		set(IGameObserver.EMPTY, IHoleCardsObserver.EMPTY);
	}

	@Override
	public Action getAction() {
		double strength = preflopEquilator.strengthVsRandom(heroCard1, heroCard2);
		if (strength > 0.5) {
			return Action.allIn();
		} else {
			return Action.fold();
		}
	}

	@Override
	public String getName() {
		return name;
	}

}
