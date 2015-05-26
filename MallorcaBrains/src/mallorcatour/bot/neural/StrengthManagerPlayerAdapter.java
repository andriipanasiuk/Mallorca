package mallorcatour.bot.neural;

import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;

public class StrengthManagerPlayerAdapter implements IPlayer{

	private final IPlayer realPlayer;
	private final StrengthManager strengthManager;
	public StrengthManagerPlayerAdapter(IPlayer realPlayer, StrengthManager strengthManager) {
		this.realPlayer = realPlayer;
		this.strengthManager = strengthManager;
	}

	@Override
	public void onHoleCards(Card c1, Card c2, String villainName) {
		strengthManager.onHoleCards(c1, c2, villainName);
		realPlayer.onHoleCards(c1, c2, villainName);
	}

	@Override
	public void onVillainActed(Action action, double toCall) {
		strengthManager.onVillainActed(action, toCall);
		realPlayer.onVillainActed(action, toCall);
	}

	@Override
	public void onStageEvent(PokerStreet street) {
		strengthManager.onStageEvent(street);
		realPlayer.onStageEvent(street);
	}

	@Override
	public void onHandStarted(IPlayerGameInfo gameInfo) {
		strengthManager.onHandStarted(gameInfo);
		realPlayer.onHandStarted(gameInfo);
	}

	@Override
	public void onHandEnded() {
		strengthManager.onHandEnded();
		realPlayer.onHandEnded();
	}

	@Override
	public Action getAction() {
		return realPlayer.getAction();
	}

	@Override
	public String getName() {
		return realPlayer.getName();
	}
	
}