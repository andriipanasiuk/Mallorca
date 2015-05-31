package mallorcatour.bot.random;

import java.util.Random;

import mallorcatour.bot.ObservingPlayer;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.interfaces.IGameObserver;

public class RandomBot extends ObservingPlayer {

	private Random random = new Random(System.currentTimeMillis());

	public RandomBot(String name, String debug) {
		super(name, debug);
		set(null, IGameObserver.EMPTY, IHoleCardsObserver.EMPTY);
	}

	@Override
	public Action getAction() {
		double random = this.random.nextDouble();
		if (gameInfo.getAmountToCall() > 0) {
			if (random < 0.33) {
				return Action.foldAction();
			} else if (random < 0.66) {
				return Action.callAction(gameInfo.getAmountToCall());
			} else {
				return Action.createRaiseAction(gameInfo.getAmountToCall(), gameInfo.getPotSize(),
						gameInfo.getBankRollAtRisk());
			}
		} else {
			if (random < 0.5) {
				return Action.checkAction();
			} else {
				return Action.createBetAction(gameInfo.getAmountToCall(), gameInfo.getPotSize(),
						gameInfo.getBankRollAtRisk());
			}
		}
	}

	@Override
	public String getDefaultName() {
		return "RandomBot";
	}

}
