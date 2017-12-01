package mallorcatour.bot.random;

import java.util.Random;

import mallorcatour.bot.ObservingPlayer;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.state.HandState;
import mallorcatour.core.game.state.observer.BaseHandStateObserver;
import mallorcatour.core.player.interfaces.Player;

public class RandomBot extends ObservingPlayer implements Player {

    private Random random = new Random(System.currentTimeMillis());

    private final BaseHandStateObserver stateObserver;

	public RandomBot(String name, String debug) {
		super(name, debug);
		stateObserver = new BaseHandStateObserver(name, true);
		set(stateObserver, IHoleCardsObserver.EMPTY);
	}

	@Override
	public Action getAction() {
		HandState state = stateObserver.getSituation();
		double toCall = state.getAmountToCall();
		double random = this.random.nextDouble();
		if (toCall > 0) {
			if (random < 0.33) {
				return Action.fold();
			} else if (random < 0.66) {
				return Action.callAction(toCall);
			} else {
				return Action.createRaiseAction(toCall, gameInfo.getPotSize(),
						gameInfo.getBankRollAtRisk());
			}
		} else {
			if (random < 0.5) {
				return Action.checkAction();
			} else {
				return Action.createRaiseAction(toCall, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize());
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}

}
