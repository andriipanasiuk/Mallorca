package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.PokerStreet;

public interface IGameObserver<P extends IGameInfo>{
	/**
	 * A new betting round has started. Not called on preflop.
	 */
	public void onStageEvent(PokerStreet street);
	/**
	 * A new game has been started.
	 * @param gi the game stat information
	 */
	public void onHandStarted(P gameInfo);
	public void onHandEnded();
}